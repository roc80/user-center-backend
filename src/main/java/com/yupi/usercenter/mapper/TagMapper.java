package com.yupi.usercenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.usercenter.model.Tag;
import com.yupi.usercenter.model.response.TagResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Claude Sonnet4
 * @description 针对表【tag(标签表)】的数据库操作Mapper
 * @see com.yupi.usercenter.model.Tag
 * @since 2025-05-05 15:26:29
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {
    /**
     * 检查标签名是否已存在
     */
    int checkTagNameExists(@Param("tagName") String tagName);

    /**
     * 检查父标签是否存在
     */
    int checkParentTagExists(@Param("parentId") Long parentId);

    /**
     * 根据用户ID和父标签ID查询子标签
     */
    List<Tag> findChildrenByParentId(@Param("userId") Long userId, @Param("parentId") Long parentId);

    /**
     * 根据用户ID查询所有根标签
     */
    List<Tag> findRootTags();

    /**
     * 更新父标签状态
     */
    void updateParentStatus(@Param("tagId") Long tagId, @Param("isParent") Integer isParent);

    /**
     * 检查标签是否有子标签
     */
    int countChildrenByParentId(@Param("parentId") Long parentId);

    /**
     * 根据ID查询标签（包含父标签名称）
     */
    TagResponse selectTagWithParentName(@Param("tagId") Long tagId);

    Long selectDeletedTagIdByName(@Param("tagName") String tagName);

    void restoreTag(@Param("id") Long id, @Param("creatorUserId") Long creatorUserId);
}




