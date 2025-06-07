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
* @since 2025-05-05 15:26:29
* @see com.yupi.usercenter.model.Tag
*/
@Mapper
public interface TagMapper extends BaseMapper<Tag> {
    /**
     * 检查标签名是否已存在（同一用户下且未删除）
     */
    int checkTagNameExists(@Param("tagName") String tagName, @Param("userId") Long userId);

    /**
     * 检查父标签是否存在且属于当前用户
     */
    int checkParentTagExists(@Param("parentId") Long parentId, @Param("userId") Long userId);

    /**
     * 根据用户ID和父标签ID查询子标签
     */
    List<Tag> findChildrenByParentId(@Param("userId") Long userId, @Param("parentId") Long parentId);

    /**
     * 根据用户ID查询所有根标签
     */
    List<Tag> findRootTagsByUserId(@Param("userId") Long userId);

    /**
     * 更新父标签状态
     */
    int updateParentStatus(@Param("tagId") Long tagId, @Param("isParent") Integer isParent);

    /**
     * 检查标签是否有子标签
     */
    int countChildrenByParentId(@Param("parentId") Long parentId);

    /**
     * 根据ID查询标签（包含父标签名称）
     */
    TagResponse selectTagWithParentName(@Param("tagId") Long tagId);
}




