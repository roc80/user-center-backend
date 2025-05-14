package com.yupi.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yupi.usercenter.constant.UserConstant;
import com.yupi.usercenter.exception.BusinessException;
import com.yupi.usercenter.mapper.UserMapper;
import com.yupi.usercenter.model.User;
import com.yupi.usercenter.model.UserDTO;
import com.yupi.usercenter.model.base.BaseResponse;
import com.yupi.usercenter.model.base.Error;
import com.yupi.usercenter.model.base.ResponseUtils;
import com.yupi.usercenter.model.helper.ModelHelper;
import com.yupi.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 针对表【user(用户表)】的数据库操作Service实现
 *
 * @author lipeng
 * @since 2024-10-21 18:42:42
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    private static final int USERNAME_MIN_LENGTH = 4;
    private static final int USERNAME_MAX_LENGTH = 256;
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int PASSWORD_MAX_LENGTH = 2048;

    // TODO@lp 不能硬编码在类中
    public static final String SUFFIX_SALT = "suARnTClqnWOx8";
    private static final String USER_LOGIN_INFO = "user_login_info";

    public BaseResponse<Long> userRegister(@NonNull String userName, @NonNull String userPassword, @NonNull String repeatPassword) {
        if (StringUtils.isAnyBlank(userName, userPassword, repeatPassword)) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "用户名或密码不能为空");
        }
        String result = commonCheck(userName, userPassword);
        if (result != null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, result);
        }
        // 密码确认校验
        if (!userPassword.equals(repeatPassword)) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "两次输入的密码不一致");
        }
        // userName 唯一
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        long count = this.count(queryWrapper.eq("user_name", userName));
        if (count > 0) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "该用户名已存在");
        }
        // 插入一个新用户，返回用户id
        String userPasswordMd5 = DigestUtils.md5DigestAsHex((userPassword + SUFFIX_SALT).getBytes());
        User newUser = new User(userName, userPasswordMd5);
        this.save(newUser);
        return ResponseUtils.success(newUser.getId());
    }

    private String commonCheck(String userName, String userPassword) {
        // 用户名长度校验
        if (userName.length() < USERNAME_MIN_LENGTH || userName.length() > USERNAME_MAX_LENGTH) {
            return "用户名长度异常";
        }
        // 用户名特殊字符校验
        String regex = "[!@#$%^&*()_+\\-={}\\[\\]:\";',.?/\\\\|]"; /*随便搜的regex */
        Matcher matcher = Pattern.compile(regex).matcher(userName);
        if (matcher.find()) {
            return "用户名不能包含特殊字符";
        }
        // 密码长度校验
        if (userPassword.length() < PASSWORD_MIN_LENGTH || userPassword.length() > PASSWORD_MAX_LENGTH) {
            return "密码长度异常";
        }
        // 如果全部校验通过，则必须返回Null
        return null;
    }

    public BaseResponse<UserDTO> userLogin(@NonNull String userName, @NonNull String userPassword, HttpServletRequest request) {
        if (StringUtils.isAnyBlank(userName, userPassword)) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "用户名或密码不能为空");
        }
        String reason = commonCheck(userName, userPassword);
        if (reason != null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, reason);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", userName);
        User savedUser = this.getOne(queryWrapper);
        if (savedUser == null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "该用户不存在");
        }
        // 密码校验
        String savedUserPasswordMD5 = savedUser.getUserPassword();
        String needCheckPasswordMD5 = DigestUtils.md5DigestAsHex((userPassword + SUFFIX_SALT).getBytes());
        if (needCheckPasswordMD5.equals(savedUserPasswordMD5)) {
            UserDTO userDTO = ModelHelper.INSTANCE.convertUserToUserDto(savedUser);
            request.getSession().setAttribute(USER_LOGIN_INFO, userDTO);
            return ResponseUtils.success(userDTO);
        } else {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "密码校验失败");
        }
    }

    @Override
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_INFO);
        return ResponseUtils.success(true);
    }

    @Override
    public BaseResponse<List<UserDTO>> searchUser(@NotNull String userName, HttpServletRequest request) {
        if (isNotAdmin(request)) {
            throw new BusinessException(Error.CLIENT_FORBIDDEN, "非管理员，无权限查询用户");
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<User>().like("user_name", userName);
        List<User> originalUserList = this.list(userQueryWrapper);
        List<UserDTO> safetyUserList = originalUserList.stream().map(ModelHelper.INSTANCE::convertUserToUserDto).collect(Collectors.toList());
        return ResponseUtils.success(safetyUserList);
    }

    @Override
    public BaseResponse<Boolean> deleteUser(@NotNull Long userId, HttpServletRequest request) {
        if (isNotAdmin(request)) {
            throw new BusinessException(Error.CLIENT_FORBIDDEN, "无权限删除用户");
        }
        boolean deleted = this.removeById(userId);
        if (!deleted) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "删除失败");
        }
        return ResponseUtils.success(true);
    }

    @Override
    public @Nullable BaseResponse<UserDTO> currentUser(HttpServletRequest request) {
        Object userLoginInfo = request.getSession().getAttribute(USER_LOGIN_INFO);
        if (!(userLoginInfo instanceof UserDTO)) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "session中存储的用户信息异常");
        }
        User userPO = this.getById(((UserDTO) (userLoginInfo)).getUserId());
        if (userPO == null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "该用户不存在");
        }
        UserDTO userDTO = ModelHelper.INSTANCE.convertUserToUserDto(userPO);
        request.getSession().setAttribute(USER_LOGIN_INFO, userDTO);
        return ResponseUtils.success(userDTO);
    }

    @Override
    public BaseResponse<List<UserDTO>> searchAllUser(HttpServletRequest request) {
        if (isNotAdmin(request)) {
            throw new BusinessException(Error.CLIENT_FORBIDDEN, "无权限查询用户");
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        List<User> originalUserList = this.list(userQueryWrapper);
        List<UserDTO> safetyUserList = originalUserList.stream().map(ModelHelper.INSTANCE::convertUserToUserDto).collect(Collectors.toList());
        return ResponseUtils.success(safetyUserList);
    }

    private boolean isNotAdmin(HttpServletRequest request) {
        UserDTO loginUser = getLoginUser(request);
        if (loginUser == null) return true;
        return !UserConstant.USER_ROLE_ADMIN.equals(loginUser.getUserRole());
    }

    /**
     * @return null or userDTO(userId > 0)
     * @author lipeng
     * @since 2025/5/15 10:45
    */
    @org.jetbrains.annotations.Nullable
    private static UserDTO getLoginUser(HttpServletRequest request) {
        if (request == null || request.getSession() == null) {
            return null;
        }
        Object userLoginInfo = request.getSession().getAttribute(USER_LOGIN_INFO);
        if (!(userLoginInfo instanceof UserDTO)) {
            return null;
        }
        UserDTO loginUser = (UserDTO) userLoginInfo;
        if (loginUser.getUserId() == null || loginUser.getUserId() <= 0) {
            return null;
        }
        return loginUser;
    }


    /**
     * @return 返回的用户包含传入的所有tag
     * @author lipeng
     * @since 2025/5/5 16:00
     */
    @Override
    public @NonNull BaseResponse<Set<UserDTO>> searchUsersByTags(@Nullable List<String> tagNameList) {
        if (tagNameList == null || tagNameList.isEmpty()) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "tags为空");
        }
        // 1.在数据库中查
//        List<User> matchedUserList = searchUsersInDB(tagNameList);
        // TODO@lp 这两种查询方式哪个好，等以后数据量起来之后，再对比
        // 2.在内存中查
        List<User> matchedUserList = searchUsersInMem(tagNameList);

        Set<UserDTO> users = matchedUserList.stream().map(ModelHelper.INSTANCE::convertUserToUserDto).collect(Collectors.toSet());
        return ResponseUtils.success(users);
    }

    @Override
    public BaseResponse<Integer> updateUser(HttpServletRequest request, @Nullable UserDTO userDTO) {
        if (userDTO == null || userDTO.getUserId() == null) {
            throw new BusinessException(Error.CLIENT_PARAMS_NULL, "");
        }
        // 校验权限
        UserDTO loginUser = getLoginUser(request);
        boolean isSameUser = loginUser != null
                && userDTO.getUserId().equals(loginUser.getUserId());
        if (isNotAdmin(request) && !isSameUser) {
            throw new BusinessException(Error.CLIENT_NO_AUTH, "无权限修改");
        }
        User partialUser = ModelHelper.INSTANCE.convertUserDtoToUser(userDTO);
        User oldUser = this.getById(partialUser.getId());
        updateUser(oldUser, partialUser);
        boolean updated = this.updateById(oldUser);
        if (updated) {
            return ResponseUtils.success(0);
        } else {
            return ResponseUtils.success(-1);
        }
    }

    /**
     * @param partialUser 前端更新用户信息后，传递的新用户对象。只有不为空的部分是需要写入数据库的。
     * @author lipeng
     * @since 2025/5/15 11:03
    */
    private void updateUser(User originalUser, User partialUser) {
        if (originalUser == null || partialUser == null) {
            return;
        }
        if (partialUser.getUserName() != null) {
            originalUser.setUserName(partialUser.getUserName());
        }
        if (partialUser.getGender() != null) {
            originalUser.setGender(partialUser.getGender());
        }
        if (partialUser.getEmail() != null) {
            originalUser.setEmail(partialUser.getEmail());
        }
        if (partialUser.getPhone() != null) {
            originalUser.setPhone(partialUser.getPhone());
        }
        if (partialUser.getEmail() != null) {
            originalUser.setEmail(partialUser.getEmail());
        }
        if (partialUser.getEmail() != null) {
            originalUser.setEmail(partialUser.getEmail());
        }
        if (partialUser.getTagJsonList() != null) {
            originalUser.setTagJsonList(partialUser.getTagJsonList());
        }
        if (partialUser.isValid() != null) {
            originalUser.setValid(partialUser.isValid());
        }
        if (partialUser.getUserRole() != null) {
            originalUser.setUserRole(partialUser.getUserRole());
        }
        originalUser.setCreateDatetime(new Date());
    }

    private @NonNull List<User> searchUsersInMem(@NonNull List<String> tagNameList) {
        List<User> userList = this.list();
        return userList.stream().filter(user -> {
            String tagJsonStr = user.getTagJsonList();
            if (tagJsonStr == null) {
                return false;
            }
            Type type = new TypeToken<Set<String>>() {
            }.getType();
            Set<String> userTagNameSet = new Gson().fromJson(tagJsonStr, type);
            for (String inputTagName : tagNameList) {
                if (!userTagNameSet.contains(inputTagName)) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());
    }

    private @NonNull List<User> searchUsersInDB(@NonNull List<String> tagNameList) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tag_json_list", tagName);
        }
        return this.list(queryWrapper);
    }

}