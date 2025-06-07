package com.yupi.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.usercenter.exception.BusinessException;
import com.yupi.usercenter.mapper.TagMapper;
import com.yupi.usercenter.model.Tag;
import com.yupi.usercenter.model.TagTreeNode;
import com.yupi.usercenter.model.base.Error;
import com.yupi.usercenter.model.request.CreateTagRequest;
import com.yupi.usercenter.model.response.TagResponse;
import com.yupi.usercenter.service.TagService;
import org.springframework.stereotype.Service;

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
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

    final TagMapper tagMapper;

    public TagServiceImpl(TagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }

    /**
     * 创建标签
     */
    public TagResponse createTag(CreateTagRequest request, Long userId) {
        // 1. 验证标签名是否重复
        if (tagMapper.checkTagNameExists(request.getTagName(), userId) > 0) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "标签名已存在");
        }

        // 2. 验证父标签逻辑
        validateParentTag(request, userId);

        // 3. 创建标签
        Tag tag = new Tag();
        tag.setTagName(request.getTagName());
        tag.setUserId(userId);
        tag.setParentId(request.getParentId());
        tag.setParent(request.isParent());
        tag.setCreateDatetime(new Date());
        tag.setUpdateDatetime(new Date());
        tag.setDelete(0);

        tagMapper.insert(tag);

        // 4. 如果创建的是子标签，需要更新父标签的 is_parent 状态
        if (request.getParentId() != null && request.getParentId() > 0) {
            updateParentTagStatus(request.getParentId());
        }

        // 5. 构建响应
        return tagMapper.selectTagWithParentName(tag.getId());
    }

    /**
     * 验证父标签逻辑
     */
    private void validateParentTag(CreateTagRequest request, Long userId) {
        Long parentId = request.getParentId();
        Integer isParent = request.isParent();

        if (parentId == null || parentId == 0) {
            // 根标签或独立标签
            if (isParent == null || (isParent != 0 && isParent != 1)) {
                throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "根标签的is_parent必须为0或1");
            }
        } else {
            // 子标签
            // 检查父标签是否存在且属于当前用户
            if (tagMapper.checkParentTagExists(parentId, userId) == 0) {
                throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "父标签不存在或不属于当前用户");
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
        if ((parentTag != null) && (parentTag.isParent() != null) && (parentTag.isParent() == 0)) {
            tagMapper.updateParentStatus(parentId, 1);
        }
    }

    /**
     * 获取用户的标签树
     */
    public List<TagTreeNode> getUserTagTree(Long userId) {
        List<Tag> rootTags = tagMapper.findRootTagsByUserId(userId);
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

        if (tag.isParent() != null && tag.isParent() == 1) {
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
    public void deleteTag(Long tagId, Long userId) {
        // 检查标签是否存在且属于当前用户
        if (tagMapper.checkParentTagExists(tagId, userId) == 0) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "标签不存在或不属于当前用户");
        }

        // 检查是否有子标签
        if (tagMapper.countChildrenByParentId(tagId) > 0) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "该标签下还有子标签，无法删除");
        }

        // 执行逻辑删除
        Tag tag = new Tag();
        tag.setId(tagId);
        tag.setDelete(1);
        tag.setUpdateDatetime(new Date());
        tagMapper.updateById(tag);
    }

    /**
     * 根据ID获取标签详情
     */
    public TagResponse getTagById(Long tagId, Long userId) {
        TagResponse tagResponse = tagMapper.selectTagWithParentName(tagId);
        if (tagResponse == null || !Objects.equals(tagResponse.getUserId(), userId)) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "标签不存在或不属于当前用户");
        }
        return tagResponse;
    }

}




