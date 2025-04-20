package com.yupi.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.usercenter.constant.UserConstant;
import com.yupi.usercenter.exception.BusinessException;
import com.yupi.usercenter.mapper.UserMapper;
import com.yupi.usercenter.model.User;
import com.yupi.usercenter.model.base.BaseResponse;
import com.yupi.usercenter.model.base.Error;
import com.yupi.usercenter.model.base.ResponseUtils;
import com.yupi.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
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

    public BaseResponse<User> userLogin(@NonNull String userName, @NonNull String userPassword, HttpServletRequest request) {
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
            User safeUser = getSafeUser(savedUser);
            request.getSession().setAttribute(USER_LOGIN_INFO, safeUser);
            return ResponseUtils.success(safeUser);
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
    public BaseResponse<List<User>> searchUser(@NotNull String userName, HttpServletRequest request) {
        if (isAdmin(request)) {
            throw new BusinessException(Error.CLIENT_FORBIDDEN, "非管理员，无权限查询用户");
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<User>().like("user_name", userName);
        List<User> originalUserList = this.list(userQueryWrapper);
        List<User> safetyUserList = originalUserList.stream().map(this::getSafeUser).collect(Collectors.toList());
        return ResponseUtils.success(safetyUserList);
    }

    @Override
    public BaseResponse<Boolean> deleteUser(@NotNull Long userId, HttpServletRequest request) {
        if (isAdmin(request)) {
            throw new BusinessException(Error.CLIENT_FORBIDDEN, "无权限删除用户");
        }
        boolean deleted = this.removeById(userId);
        if (!deleted) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "删除失败");
        }
        return ResponseUtils.success(true);
    }

    @Override
    public @Nullable BaseResponse<User> currentUser(HttpServletRequest request) {
        Object userLoginInfo = request.getSession().getAttribute(USER_LOGIN_INFO);
        if (!(userLoginInfo instanceof User)) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "session中存储的用户信息异常");
        }
        User user = this.getById(((User) (userLoginInfo)).getId());
        if (user == null) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "该用户不存在");
        }
        request.getSession().setAttribute(USER_LOGIN_INFO, user);
        return ResponseUtils.success(getSafeUser(user));
    }

    @Override
    public BaseResponse<List<User>> searchAllUser(HttpServletRequest request) {
        if (isAdmin(request)) {
            throw new BusinessException(Error.CLIENT_FORBIDDEN, "无权限查询用户");
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        List<User> originalUserList = this.list(userQueryWrapper);
        List<User> safetyUserList = originalUserList.stream().map(this::getSafeUser).collect(Collectors.toList());
        return ResponseUtils.success(safetyUserList);
    }

    private boolean isAdmin(HttpServletRequest request) {
        if (request == null || request.getSession() == null) {
            return true;
        }
        Object userLoginInfo = request.getSession().getAttribute(USER_LOGIN_INFO);
        if (!(userLoginInfo instanceof User)) {
            return true;
        }
        return ((User) userLoginInfo).getUserRole() != UserConstant.USER_ROLE_ADMIN;
    }

    /**
     * 获取脱敏用户信息
     */
    private @NonNull User getSafeUser(@NonNull User originalUser) {
        User safeUser = new User(originalUser.getUserName(), "");
        safeUser.setId(originalUser.getId());
        safeUser.setAvatarUrl(originalUser.getAvatarUrl());
        safeUser.setGender(originalUser.getGender());
        safeUser.setPhone(originalUser.getPhone());
        safeUser.setEmail(originalUser.getEmail());
        safeUser.setCreateDatetime(originalUser.getCreateDatetime());
        safeUser.setIsValid(originalUser.getIsValid());
        safeUser.setUserRole(originalUser.getUserRole());
        return safeUser;
    }

}