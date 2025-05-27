package com.yupi.usercenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.usercenter.model.Team;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author lipeng
* @description 针对表【team】的数据库操作Mapper
* @createDate 2025-05-23 10:09:11
* @Entity com.yupi.usercenter.model.Team
*/
@Mapper
public interface TeamMapper extends BaseMapper<Team> {

    int updateTeamMemberIds(@Param("teamId") Long teamId, @Param("memberIds") String memberIds);

    int updateTeamOwner(@Param("teamId") Long teamId, @Param("nextOwnerUserId") long nextOwnerUserId);

    int selectTeamsByOwnerUserId(@Param("ownerUserId") Long ownerUserId);

    List<Team> selectAllTeam(@Param("queryAllJoinType") boolean queryAllJoinType, @Param("privateJoinType") int privateJoinType);

    List<Team> selectTeamByPage(@Param("queryAllJoinType") boolean queryAllJoinType,
                                @Param("privateJoinType") int privateJoinType,
                                @Param("offset") int offset,
                                @Param("limit") int limit);
}




