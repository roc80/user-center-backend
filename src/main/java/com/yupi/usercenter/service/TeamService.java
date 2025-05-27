package com.yupi.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.usercenter.model.Team;
import com.yupi.usercenter.model.base.BaseResponse;
import com.yupi.usercenter.model.dto.TeamDTO;
import com.yupi.usercenter.model.request.TeamCreateRequest;
import com.yupi.usercenter.model.request.TeamUpdateRequest;
import com.yupi.usercenter.model.request.UserExitTeamRequest;
import com.yupi.usercenter.model.request.UserJoinTeamRequest;
import org.springframework.lang.NonNull;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author lipeng
* @description 针对表【team】的数据库操作Service
* @createDate 2025-05-23 10:09:11
*/
public interface TeamService extends IService<Team> {

    BaseResponse<Boolean> createTeam(TeamCreateRequest request, @NonNull Long userId);

    BaseResponse<List<TeamDTO>> retrieveTeams(HttpServletRequest request);

    BaseResponse<List<TeamDTO>> retrieveTeamsByPage(HttpServletRequest request, int pageNum, int pageSize);

    BaseResponse<Boolean> updateTeam(HttpServletRequest httpServletRequest, TeamUpdateRequest teamUpdateRequest);

    BaseResponse<Boolean> deleteTeam(HttpServletRequest request, Long teamId);

    BaseResponse<Boolean> userJoinTeam(HttpServletRequest request, @NonNull Long teamId, UserJoinTeamRequest userJoinTeamRequest);

    BaseResponse<Boolean> userExitTeam(HttpServletRequest request, Long teamId, Long userId, UserExitTeamRequest userExitTeamRequest);
}
