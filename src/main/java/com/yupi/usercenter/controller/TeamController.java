package com.yupi.usercenter.controller;

import com.yupi.usercenter.exception.BusinessException;
import com.yupi.usercenter.model.base.BaseResponse;
import com.yupi.usercenter.model.base.Error;
import com.yupi.usercenter.model.dto.UserDTO;
import com.yupi.usercenter.model.request.TeamCreateRequest;
import com.yupi.usercenter.service.TeamService;
import com.yupi.usercenter.utils.UserHelper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


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
        Long userId;
        if ((userId = userDTO.getUserId()) == null) {
            throw new BusinessException(Error.CLIENT_NO_AUTH, "");
        }
        return teamService.createTeam(teamCreateRequest, userId);
    }


//    @PostMapping("/retrieve")
//    public BaseResponse<List<Team>> retrieveTeams(HttpServletRequest request) {
//
//    }
//
//
//    @PostMapping("/update")
//    public BaseResponse<Boolean> updateTeam(HttpServletRequest request) {
//
//    }
//
//
//    @PostMapping("/delete")
//    public BaseResponse<Boolean> deleteTeam(HttpServletRequest request) {
//
//    }


}
