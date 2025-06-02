package com.yupi.usercenter.controller;

import com.yupi.usercenter.exception.BusinessException;
import com.yupi.usercenter.model.base.BaseResponse;
import com.yupi.usercenter.model.base.Error;
import com.yupi.usercenter.model.dto.TeamDTO;
import com.yupi.usercenter.model.dto.UserDTO;
import com.yupi.usercenter.model.request.TeamCreateRequest;
import com.yupi.usercenter.model.request.TeamDeleteRequest;
import com.yupi.usercenter.model.request.TeamUpdateRequest;
import com.yupi.usercenter.model.request.UserJoinTeamRequest;
import com.yupi.usercenter.service.TeamService;
import com.yupi.usercenter.utils.UserHelper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;


/**
 *
 * @author lipeng
 * @since 2025/5/23 10:53
*/
@RestController
@RequestMapping("/team")
public class TeamController {

    @Resource
    TeamService teamService;


    @PostMapping("/create")
    public BaseResponse<Boolean> createTeam(HttpServletRequest httpServletRequest, @RequestBody TeamCreateRequest teamCreateRequest) {
        UserDTO userDTO = UserHelper.getUserDtoFromRequest(httpServletRequest);
        return teamService.createTeam(teamCreateRequest, Objects.requireNonNull(userDTO.getUserId()));
    }


    @GetMapping("/")
    public BaseResponse<List<TeamDTO>> retrieveTeams(HttpServletRequest request) {
        boolean isAdmin = UserHelper.isAdmin(UserHelper.getUserDtoFromRequest(request));
        return teamService.retrieveTeams(isAdmin);
    }

    @GetMapping("/page")
    public BaseResponse<List<TeamDTO>> retrieveTeamsByPage(HttpServletRequest request, Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum <= 0 || pageSize == null || pageSize <= 0) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "分页查询参数错误");
        }
        return teamService.retrieveTeamsByPage(request, pageNum, pageSize);
    }

    @GetMapping("/my/owned")
    public BaseResponse<List<TeamDTO>> retrieveMyOwnedTeams(HttpServletRequest request) {
        UserDTO loginUser = UserHelper.getUserDtoFromRequest(request);
        Long userId = loginUser.getUserId();
        if (userId == null || userId <= 0) {
            throw new BusinessException(Error.CLIENT_NO_AUTH, "");
        }
        return teamService.retrieveTeamsOwnedByUser(userId);
    }

    @GetMapping("/my/joined")
    public BaseResponse<List<TeamDTO>> retrieveMyJoinedTeams(HttpServletRequest request) {
        UserDTO loginUser = UserHelper.getUserDtoFromRequest(request);
        Long userId = loginUser.getUserId();
        if (userId == null || userId <= 0) {
            throw new BusinessException(Error.CLIENT_NO_AUTH, "");
        }
        return teamService.retrieveTeamsWhereUserIsMember(userId);
    }

    @GetMapping("/id/{id}")
    public BaseResponse<TeamDTO> retrieveTeamById(HttpServletRequest request, @PathVariable Long id) {
        UserHelper.getUserDtoFromRequest(request);
        if (id == null || id <= 0) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "");
        }
        return teamService.retrieveTeamById(id);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(HttpServletRequest httpServletRequest, @RequestBody TeamUpdateRequest teamUpdateRequest) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "");
        }
        return teamService.updateTeam(httpServletRequest, teamUpdateRequest);
    }


    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(HttpServletRequest request, @RequestBody TeamDeleteRequest teamDeleteRequest) {
        long teamId = teamDeleteRequest.getTeamId();
        if (teamId <= 0) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "");
        }
        return teamService.deleteTeam(request, teamId);
    }

    @PostMapping("/{teamId}/member")
    public BaseResponse<Boolean> addUserInTeam(HttpServletRequest request, @PathVariable Long teamId, @RequestBody(required = false) UserJoinTeamRequest userJoinTeamRequest) {
        if (teamId <= 0) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "");
        }
        return teamService.addUserInTeam(request, teamId, userJoinTeamRequest);
    }

    @DeleteMapping("/{teamId}/member/{userId}")
    public BaseResponse<Boolean> removeUserFromTeam(HttpServletRequest request,
                                              @PathVariable Long teamId,
                                              @PathVariable Long userId,
                                              @RequestParam(required = false) Long nextOwnerUserId) {
        if (teamId <= 0 || userId <= 0) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "");
        }
        return teamService.removeUserFromTeam(request, teamId, userId, nextOwnerUserId);
    }

}
