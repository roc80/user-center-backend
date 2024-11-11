package com.yupi.usercenter.controller;

import com.yupi.usercenter.model.UserLoginRequest;
import com.yupi.usercenter.model.UserRegisterRequest;
import com.yupi.usercenter.model.response.LoginUserRsp;
import com.yupi.usercenter.model.response.RegisterUserRsp;
import com.yupi.usercenter.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
