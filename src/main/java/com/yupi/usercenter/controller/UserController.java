package com.yupi.usercenter.controller;

import com.yupi.usercenter.model.request.UserDeleteRequest;
import com.yupi.usercenter.model.request.UserLoginRequest;
import com.yupi.usercenter.model.request.UserRegisterRequest;
import com.yupi.usercenter.model.response.CommonResponse;
import com.yupi.usercenter.model.response.LoginUserRsp;
import com.yupi.usercenter.model.response.RegisterUserRsp;
import com.yupi.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/register")
    public RegisterUserRsp userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            return new RegisterUserRsp("请求体为空");
        }
        return userService.userRegister(userRegisterRequest.getUserName(), userRegisterRequest.getUserPassword(), userRegisterRequest.getRepeatPassword());
    }

    @PostMapping("/login")
    public LoginUserRsp userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return new LoginUserRsp(null, "请求体为空");
        }
        return userService.userLogin(userLoginRequest.getUserName(), userLoginRequest.getUserPassword(), request);
    }

    @GetMapping("/search")
    public CommonResponse searchUser(String userName, HttpServletRequest request) {
        if (StringUtils.isBlank(userName)) {
            return new CommonResponse(-1, "用户名不能为空", null);
        }
        return userService.searchUser(userName, request);
    }

    @PostMapping("/delete")
    public CommonResponse deleteUser(@RequestBody UserDeleteRequest userDeleteRequest, HttpServletRequest request) {
        long userId = userDeleteRequest.getUserId();
        if (userId <= 0) {
            return new CommonResponse(-1, "userId不合法", null);
        }
        return userService.deleteUser(userId, request);
    }

}
