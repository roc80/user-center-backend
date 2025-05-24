package com.yupi.usercenter.controller;

import com.yupi.usercenter.exception.BusinessException;
import com.yupi.usercenter.model.base.BaseResponse;
import com.yupi.usercenter.model.base.Error;
import com.yupi.usercenter.model.dto.TeamDTO;
import com.yupi.usercenter.model.dto.UserDTO;
import com.yupi.usercenter.model.request.TeamCreateRequest;
import com.yupi.usercenter.model.request.TeamUpdateRequest;
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
@CrossOrigin(
        origins = {"http://localhost:5173"},
        allowCredentials = "true",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                RequestMethod.DELETE, RequestMethod.OPTIONS},
        maxAge = 3600                      // 预检请求的有效期
)
public class TeamController {

    @Resource
    TeamService teamService;


    @PostMapping("/create")
    public BaseResponse<Boolean> createTeam(HttpServletRequest httpServletRequest, @RequestBody TeamCreateRequest teamCreateRequest) {
        UserDTO userDTO = UserHelper.getUserDtoFromRequest(httpServletRequest);
        return teamService.createTeam(teamCreateRequest, Objects.requireNonNull(userDTO.getUserId()));
    }


    @GetMapping("/retrieve")
    public BaseResponse<List<TeamDTO>> retrieveTeams(HttpServletRequest request) {
        return teamService.retrieveTeams(request);
    }

    @GetMapping("/retrieve/page")
    public BaseResponse<List<TeamDTO>> retrieveTeamsByPage(HttpServletRequest request, int pageNum, int pageSize) {
        if (pageNum <= 0 || pageSize <= 0) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "分页查询参数错误");
        }
        return teamService.retrieveTeamsByPage(request, pageNum, pageSize);
    }


    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(HttpServletRequest httpServletRequest, @RequestBody TeamUpdateRequest teamUpdateRequest) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "");
        }
        return teamService.updateTeam(httpServletRequest, teamUpdateRequest);
    }


    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(HttpServletRequest request, Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "");
        }
        return teamService.deleteTeam(request, teamId);
    }

    @PostMapping("/{teamId}/members")
    public BaseResponse<Boolean> userJoinTeam(HttpServletRequest request, @PathVariable Long teamId) {
        // TODO@lp 用户加入队伍
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    public BaseResponse<Boolean> userExitTeam(HttpServletRequest request, @PathVariable Long teamId, @PathVariable Long userId) {
        // TODO@lp 用户退出队伍
    }

}
