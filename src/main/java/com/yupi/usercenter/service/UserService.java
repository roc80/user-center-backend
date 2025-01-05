package com.yupi.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.usercenter.model.User;
import com.yupi.usercenter.model.response.CommonResponse;
import com.yupi.usercenter.model.response.LoginUserRsp;
import com.yupi.usercenter.model.response.RegisterUserRsp;
import org.springframework.lang.NonNull;

import javax.servlet.http.HttpServletRequest;

/**
* @author lipeng
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2024-10-21 18:42:42
*/
public interface UserService extends IService<User> {

    RegisterUserRsp userRegister(@NonNull String userName, @NonNull String userPassword, @NonNull String repeatPassword);

    LoginUserRsp userLogin(@NonNull String userName, @NonNull String userPassword, HttpServletRequest request);

    CommonResponse searchUser(@NonNull String userName, HttpServletRequest request);

    CommonResponse deleteUser(@NonNull Long userId, HttpServletRequest request);
}
