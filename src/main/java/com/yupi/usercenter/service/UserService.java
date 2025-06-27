package com.yupi.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.usercenter.model.User;
import com.yupi.usercenter.model.base.BaseResponse;
import com.yupi.usercenter.model.dto.TagDTO;
import com.yupi.usercenter.model.dto.UserDTO;
import com.yupi.usercenter.model.request.TagBindRequest;
import com.yupi.usercenter.model.request.UserLoginRequest;
import com.yupi.usercenter.model.response.LoginResponse;
import com.yupi.usercenter.model.response.PageResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 针对表【user(用户表)】的数据库操作Service
* @author lipeng
* @since 2024-10-21 18:42:42
*/
public interface UserService extends IService<User> {

    BaseResponse<Long> userRegister(@NonNull String userName, @NonNull String userPassword, @NonNull String repeatPassword);

    BaseResponse<LoginResponse> userLogin(UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest);

    BaseResponse<Boolean> userLogout(HttpServletRequest request);



    BaseResponse<Boolean> deleteUser(@NonNull Long userId, HttpServletRequest request);



    BaseResponse<Integer> updateUser(HttpServletRequest request, @Nullable UserDTO userDTO);


    BaseResponse<UserDTO> currentUser(HttpServletRequest request);

    BaseResponse<UserDTO> searchUserByUserId(@NonNull Long userId);

    BaseResponse<List<UserDTO>> searchUserByUserName(@NonNull String userName, HttpServletRequest request);

    BaseResponse<PageResponse<UserDTO>> searchAllUser(HttpServletRequest request, int pageNum, int pageSize);

    BaseResponse<List<UserDTO>> searchUsersByTags(List<Long> tagIdList);

    BaseResponse<PageResponse<UserDTO>> recommendUsers(HttpServletRequest request, int pageNum, int pageSize);

    BaseResponse<List<TagDTO>> getUserTags(Long userId);

     /**
      *
      * @return tagBindRequest 中 tagIdList 有多少个已经和当前用户绑定
      */
    BaseResponse<Integer> updateTags(HttpServletRequest request, TagBindRequest tagBindRequest);

    BaseResponse<String> uploadAvatar(MultipartFile file, Long userId, HttpServletRequest request);
}
