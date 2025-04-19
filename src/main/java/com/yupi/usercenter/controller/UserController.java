package com.yupi.usercenter.controller;

import com.yupi.usercenter.model.User;
import com.yupi.usercenter.model.base.BaseResponse;
import com.yupi.usercenter.model.base.Error;
import com.yupi.usercenter.model.base.ResponseUtils;
import com.yupi.usercenter.model.request.UserDeleteRequest;
import com.yupi.usercenter.model.request.UserLoginRequest;
import com.yupi.usercenter.model.request.UserRegisterRequest;
import com.yupi.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            return ResponseUtils.error(Error.CLIENT_PARAMS_ERROR, "请求体为空");
        }
        return userService.userRegister(userRegisterRequest.getUserName(), userRegisterRequest.getUserPassword(), userRegisterRequest.getRepeatPassword());
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return ResponseUtils.error(Error.CLIENT_PARAMS_ERROR, "请求体为空");
        }
        return userService.userLogin(userLoginRequest.getUserName(), userLoginRequest.getUserPassword(), request);
    }

    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        return userService.userLogout(request);
    }

    @GetMapping("/current")
    public BaseResponse<User> currentUser(HttpServletRequest request) {
        return userService.currentUser(request);
    }


    @GetMapping("/search")
    public BaseResponse<List<User>> searchUser(String userName, HttpServletRequest request) {
        if (StringUtils.isBlank(userName)) {
            return ResponseUtils.error(Error.CLIENT_PARAMS_ERROR, "用户名不能为空");
        }
        return userService.searchUser(userName, request);
    }

    @GetMapping("/search/all")
    public BaseResponse<List<User>> searchAllUser(HttpServletRequest request) {
        return userService.searchAllUser(request);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody UserDeleteRequest userDeleteRequest, HttpServletRequest request) {
        long userId = userDeleteRequest.getUserId();
        if (userId <= 0) {
            return ResponseUtils.error(Error.CLIENT_PARAMS_ERROR, "userId不合法");
        }
        return userService.deleteUser(userId, request);
    }

}
