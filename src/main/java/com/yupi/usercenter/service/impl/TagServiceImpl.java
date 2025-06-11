package com.yupi.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.usercenter.exception.BusinessException;
import com.yupi.usercenter.mapper.TagMapper;
import com.yupi.usercenter.model.Tag;
import com.yupi.usercenter.model.TagTreeNode;
import com.yupi.usercenter.model.base.BaseResponse;
import com.yupi.usercenter.model.base.Error;
import com.yupi.usercenter.model.base.ResponseUtils;
import com.yupi.usercenter.model.request.CreateTagRequest;
import com.yupi.usercenter.model.response.TagResponse;
import com.yupi.usercenter.service.TagService;
import com.yupi.usercenter.utils.UserHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Claude Sonnet4
 * @description 针对表【tag(标签表)】的数据库操作Service实现
 * @since 2025-05-05 15:26:29
 */
@Service
@Slf4j
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
        implements TagService {

    final TagMapper tagMapper;

    public TagServiceImpl(TagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }

    /**
     * 创建标签
     */
    @Transactional(propagation = Propagation.REQUIRED, timeout = 3, rollbackFor = Exception.class)
    public BaseResponse<TagResponse> createTag(CreateTagRequest request, Long creatorUserId) {
        // 查看已删除的标签，走数据复用流程
        Long tagId = tagMapper.selectDeletedTagIdByName(request.getTagName());
        if (tagId != null) {
            // 恢复这条 tag
            tagMapper.restoreTag(tagId, creatorUserId);
            TagResponse tagResponse = tagMapper.selectTagWithParentName(tagId);
            return ResponseUtils.success(tagResponse);
        } else {
            // 验证标签名是否重复
            if (tagMapper.checkTagNameExists(request.getTagName()) > 0) {
                throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "标签名已存在");
            }

            validateParentTag(request);

            Tag tag = new Tag();
            tag.setTagName(request.getTagName());
            tag.setUserId(creatorUserId);
            tag.setParentId(request.getParentId());
            tag.setParent(request.isParent());
            tag.setCreateDatetime(new Date());
            tag.setUpdateDatetime(new Date());
            tag.setDelete(0);

            int inserted = tagMapper.insert(tag);
            log.info("插入Tag: {}, {}", tag.getTagName(), (inserted == 1 ? "插入成功" : "插入失败"));

            // 如果创建的是子标签，需要更新父标签的 is_parent 状态
            if (request.getParentId() != null && request.getParentId() > 0) {
                updateParentTagStatus(request.getParentId());
            }

            TagResponse tagResponse = tagMapper.selectTagWithParentName(tag.getId());
            return ResponseUtils.success(tagResponse);
        }
    }

    /**
     * 验证父标签逻辑
     */
    private void validateParentTag(CreateTagRequest request) {
        Long parentId = request.getParentId();
        Integer isParent = request.isParent();

        if (parentId == null || parentId == 0) {
            // 根标签或独立标签
            if (isParent == null || (isParent != 0 && isParent != 1)) {
                throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "根标签的is_parent必须为0或1");
            }
        } else {
            // 子标签
            // 检查父标签是否存在
            Long fatherId = tagMapper.selectParentId(parentId);
            if (fatherId == null) {
                throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "父标签不存在");
            }
            // 业务暂定前端只展示一层嵌套，这里需要校验 parentId 必须是根标签
            Long grandpaId = this.getById(fatherId).getParentId();
            if (grandpaId != null && grandpaId > 0) {
                throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "不允许创建三层及以上的标签");
            }

            // 子标签的is_parent可以是0或1
            if (isParent == null || (isParent != 0 && isParent != 1)) {
                throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "子标签的is_parent必须为0或1");
            }
        }
    }

    /**
     * 更新父标签状态
     * 当为某个标签添加子标签时，该标签应该被标记为父标签
     */
    private void updateParentTagStatus(Long parentId) {
        Tag parentTag = tagMapper.selectById(parentId);
        if (parentTag == null) {
            return;
        }
        if (Objects.equals(parentTag.isParent(), 0)) {
            tagMapper.updateParentStatus(parentId, 1);
        }
    }

    /**
     * 获取用户的标签树
     */
    public List<TagTreeNode> getUserTagTree() {
        List<Tag> rootTags = tagMapper.findRootTags();
        return rootTags.stream()
                .map(this::buildTagTree)
                .collect(Collectors.toList());
    }

    /**
     * 递归构建标签树
     */
    private TagTreeNode buildTagTree(Tag tag) {
        TagTreeNode node = new TagTreeNode(
                tag.getId(),
                tag.getTagName(),
                tag.isParent(),
                null
        );

        if (new Integer(1).equals(tag.isParent())) {
            List<Tag> children = tagMapper.findChildrenByParentId(tag.getUserId(), tag.getId());
            List<TagTreeNode> childNodes = children.stream()
                    .map(this::buildTagTree)
                    .collect(Collectors.toList());
            node = new TagTreeNode(
                    node.getId(),
                    node.getTagName(),
                    node.isParent(),
                    childNodes
            );
        } else {
            node = new TagTreeNode(
                    node.getId(),
                    node.getTagName(),
                    node.isParent(),
                    new ArrayList<>()
            );
        }

        return node;
    }

    /**
     * 删除标签（逻辑删除）
     */
    public BaseResponse<Boolean> deleteTag(Long tagId, HttpServletRequest request) {
        if (!UserHelper.isAdmin(UserHelper.getUserDtoFromRequest(request))) {
            throw new BusinessException(Error.CLIENT_FORBIDDEN, "管理员可以删除tag");
        }

        if (this.getById(tagId) == null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "标签不存在");
        }

        if (tagMapper.countChildrenByParentId(tagId) > 0) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "该标签下还有子标签，无法删除");
        }

        // 执行逻辑删除
        Tag tag = new Tag();
        tag.setId(tagId);
        tag.setDelete(1);
        tag.setUpdateDatetime(new Date());
        boolean isDeleted = this.removeById(tag);
        if (isDeleted) {
            return ResponseUtils.success(true);
        } else {
            return ResponseUtils.error(Error.SERVER_ERROR, "删除失败");
        }
    }

    /**
     * 根据ID获取标签详情
     */
    public TagResponse getTagById(Long tagId) {
        return tagMapper.selectTagWithParentName(tagId);
    }

}




