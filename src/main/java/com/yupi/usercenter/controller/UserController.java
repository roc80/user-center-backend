package com.yupi.usercenter.controller;

import com.yupi.usercenter.exception.BusinessException;
import com.yupi.usercenter.model.base.BaseResponse;
import com.yupi.usercenter.model.base.Error;
import com.yupi.usercenter.model.dto.TagDTO;
import com.yupi.usercenter.model.dto.UserDTO;
import com.yupi.usercenter.model.request.TagBindRequest;
import com.yupi.usercenter.model.request.UserDeleteRequest;
import com.yupi.usercenter.model.request.UserLoginRequest;
import com.yupi.usercenter.model.request.UserRegisterRequest;
import com.yupi.usercenter.model.response.LoginResponse;
import com.yupi.usercenter.model.response.PageResponse;
import com.yupi.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
        return userService.userRegister(userRegisterRequest.getUsername(), userRegisterRequest.getPassword(), userRegisterRequest.getRepeatPassword());
    }

    @PostMapping("/login")
    public BaseResponse<LoginResponse> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest) {
        return userService.userLogin(userLoginRequest, httpServletRequest);
    }

    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        return userService.userLogout(request);
    }

    @GetMapping("/current")
    public BaseResponse<UserDTO> currentUser(HttpServletRequest request) {
        return userService.currentUser(request);
    }


    @GetMapping("/search")
    public BaseResponse<List<UserDTO>> searchUserByUserName(String userName, HttpServletRequest request) {
        if (StringUtils.isBlank(userName)) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "用户名为空");
        }
        return userService.searchUserByUserName(userName, request);
    }

    @GetMapping("/search/all")
    public BaseResponse<PageResponse<UserDTO>> searchAllUser(HttpServletRequest request, Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum <= 0 || pageSize == null || pageSize <= 0) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "分页参数错误");
        }
        return userService.searchAllUser(request, pageNum, pageSize);
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<UserDTO>> searchUserByTagIds(@RequestParam("tagIds") List<Long> tagIdList) {
        if (tagIdList == null || tagIdList.isEmpty()) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "TagId不能为空");
        }
        return userService.searchUsersByTags(tagIdList);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody UserDeleteRequest userDeleteRequest, HttpServletRequest request) {
        long userId = userDeleteRequest.getUserId();
        if (userId <= 0) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "userId不合法");
        }
        return userService.deleteUser(userId, request);
    }

    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(HttpServletRequest request, @RequestBody UserDTO userDTO) {
        if (userDTO == null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "要更新的用户为空");
        }
        return userService.updateUser(request, userDTO);
    }

    @GetMapping("/recommend")
    public BaseResponse<PageResponse<UserDTO>> recommendUsers(HttpServletRequest request, Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum <= 0 || pageSize == null || pageSize <= 0) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "分页参数错误");
        }
        return userService.recommendUsers(request, pageNum, pageSize);
    }

    @GetMapping("/{userId}/tags")
    public BaseResponse<List<TagDTO>> getUserTags(@PathVariable Long userId) {
        return userService.getUserTags(userId);
    }

     /**
      * @return tagIdList有几个已经添加到当前用户了
      */
    @PostMapping("/my/tags")
    public BaseResponse<Integer> updateTags(HttpServletRequest request, @RequestBody TagBindRequest tagBindRequest) {
        return userService.updateTags(request, tagBindRequest);
    }

    @PostMapping("/avatar")
    public BaseResponse<Integer> uploadAvatar(
            @RequestParam("file")MultipartFile file,
            @RequestParam(value = "userId", defaultValue = "0")Long userId,
            HttpServletRequest request
    ) {
        return userService.uploadAvatar(file, userId, request);
    }
}
