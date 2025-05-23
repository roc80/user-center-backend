package com.yupi.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.usercenter.constant.TeamConstant;
import com.yupi.usercenter.exception.BusinessException;
import com.yupi.usercenter.mapper.TeamMapper;
import com.yupi.usercenter.model.Team;
import com.yupi.usercenter.model.base.BaseResponse;
import com.yupi.usercenter.model.base.Error;
import com.yupi.usercenter.model.base.ResponseUtils;
import com.yupi.usercenter.model.enums.TeamTypeEnum;
import com.yupi.usercenter.model.request.TeamCreateRequest;
import com.yupi.usercenter.service.TeamService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

/**
* @author lipeng
* @description 针对表【team】的数据库操作Service实现
* @createDate 2025-05-23 10:09:11
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

    @Override
    public BaseResponse<Boolean> createTeam(TeamCreateRequest teamCreateRequest, @NotNull Long userId) {
        TeamTypeEnum teamTypeEnum = getTeamTypeEnum(teamCreateRequest.getJoinType());
        if (teamTypeEnum == TeamTypeEnum.SECRET && teamCreateRequest.getJoinKey() == null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "加密队伍必须要有密钥");
        }
        int maxNum = teamCreateRequest.getMaxNum();
        if (maxNum > TeamConstant.TEAM_MEMBER_NUM_LIMIT || maxNum <= 0) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "最大人数错误");
        }
//        查询已创建队伍数量，要<=5
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.eq("owner_user_id", userId);
        long currentUserOwnedTeams = this.count(teamQueryWrapper);
        if (currentUserOwnedTeams >= 5) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "当前用户拥有的队伍数量超过5个，不允许继续创建!");
        }

        Team team = new Team(teamCreateRequest, userId);
        boolean saved = this.save(team);
        return ResponseUtils.success(saved);
    }

    private TeamTypeEnum getTeamTypeEnum(int joinType) {
        TeamTypeEnum teamTypeEnum = TeamTypeEnum.Companion.fromValue(joinType);
        if (teamTypeEnum == null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "加入方式错误");
        }
        return teamTypeEnum;
    }
}




