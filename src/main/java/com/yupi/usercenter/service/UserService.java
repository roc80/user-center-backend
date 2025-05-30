package com.yupi.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.usercenter.model.User;
import com.yupi.usercenter.model.base.BaseResponse;
import com.yupi.usercenter.model.dto.UserDTO;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/**
 * 针对表【user(用户表)】的数据库操作Service
* @author lipeng
* @since 2024-10-21 18:42:42
*/
public interface UserService extends IService<User> {

    BaseResponse<Long> userRegister(@NonNull String userName, @NonNull String userPassword, @NonNull String repeatPassword);

    BaseResponse<UserDTO> userLogin(@NonNull String userName, @NonNull String userPassword, HttpServletRequest request);

    BaseResponse<Boolean> userLogout(HttpServletRequest request);



    BaseResponse<Boolean> deleteUser(@NonNull Long userId, HttpServletRequest request);



    BaseResponse<Integer> updateUser(HttpServletRequest request, @Nullable UserDTO userDTO);


    BaseResponse<UserDTO> currentUser(HttpServletRequest request);

    BaseResponse<UserDTO> searchUserByUserId(@NonNull Long userId);

    BaseResponse<List<UserDTO>> searchUserByUserName(@NonNull String userName, HttpServletRequest request);

    BaseResponse<List<UserDTO>> searchAllUser(HttpServletRequest request, int pageNum, int pageSize);

    BaseResponse<Set<UserDTO>> searchUsersByTags(List<String> tagNameList);

    BaseResponse<List<UserDTO>> recommendUsers(HttpServletRequest request, int pageNum, int pageSize);
}
