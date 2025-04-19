package com.yupi.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.usercenter.model.User;
import com.yupi.usercenter.model.base.BaseResponse;
import org.springframework.lang.NonNull;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 针对表【user(用户表)】的数据库操作Service
* @author lipeng
* @since 2024-10-21 18:42:42
*/
public interface UserService extends IService<User> {

    BaseResponse<Long> userRegister(@NonNull String userName, @NonNull String userPassword, @NonNull String repeatPassword);

    BaseResponse<User> userLogin(@NonNull String userName, @NonNull String userPassword, HttpServletRequest request);

    BaseResponse<List<User>> searchUser(@NonNull String userName, HttpServletRequest request);

    BaseResponse<Boolean> deleteUser(@NonNull Long userId, HttpServletRequest request);

    BaseResponse<User> currentUser(HttpServletRequest request);

    BaseResponse<List<User>> searchAllUser(HttpServletRequest request);

    BaseResponse<Boolean> userLogout(HttpServletRequest request);
}
