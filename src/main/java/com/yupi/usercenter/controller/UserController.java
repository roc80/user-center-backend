package com.yupi.usercenter.controller;

import com.yupi.usercenter.exception.BusinessException;
import com.yupi.usercenter.model.base.BaseResponse;
import com.yupi.usercenter.model.base.Error;
import com.yupi.usercenter.model.dto.UserDTO;
import com.yupi.usercenter.model.request.UserDeleteRequest;
import com.yupi.usercenter.model.request.UserLoginRequest;
import com.yupi.usercenter.model.request.UserRegisterRequest;
import com.yupi.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/user")
@CrossOrigin(
        origins = {"http://localhost:5173"},
        allowCredentials = "true",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                RequestMethod.DELETE, RequestMethod.OPTIONS},
        maxAge = 3600                      // 预检请求的有效期
)
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "请求体为空");
        }
        return userService.userRegister(userRegisterRequest.getUsername(), userRegisterRequest.getPassword(), userRegisterRequest.getRepeatPassword());
    }

    @PostMapping("/login")
    public BaseResponse<UserDTO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "请求体为空");
        }
        return userService.userLogin(userLoginRequest.getUsername(), userLoginRequest.getPassword(), request);
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
    public BaseResponse<List<UserDTO>> searchAllUser(HttpServletRequest request, int pageNum, int pageSize) {
        if (pageNum <= 0 || pageSize <= 0) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "分页参数错误");
        }
        return userService.searchAllUser(request, pageNum, pageSize);
    }

    @GetMapping("/search/tags")
    public BaseResponse<Set<UserDTO>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (tagNameList == null || tagNameList.isEmpty()) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "Tags不能为空");
        }
        return userService.searchUsersByTags(tagNameList);
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
    public BaseResponse<List<UserDTO>> recommendUsers(HttpServletRequest request, Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum <= 0 || pageSize == null || pageSize <= 0) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "分页参数错误");
        }
        return userService.recommendUsers(request, pageNum, pageSize);
    }
}
