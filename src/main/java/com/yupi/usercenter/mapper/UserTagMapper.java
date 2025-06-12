package com.yupi.usercenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.usercenter.model.UserTag;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author lipeng
* @description 针对表【user_tag】的数据库操作Mapper
* @since 2025-06-12 09:03:55
* @see com.yupi.usercenter.model.UserTag
*/
public interface UserTagMapper extends BaseMapper<UserTag> {

    List<Long> selectUserIdByTagIdList(@Param("tagIdList") List<Long> tagIdList, @Param("tagCount") int tagCount);

    List<Long> selectTagIdByUserId(@Param("userId") Long userId);

    Integer restoreDeletedOneByUniqueId(@Param("userId") Long userId, @Param("tagId") Long tagId);

    Integer logicDeleteByUniqueId(@Param("userId") Long userId, @Param("tagId") Long tagId);

    List<Long> selectUserIdWithTags();
}




