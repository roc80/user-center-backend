package com.yupi.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.usercenter.constant.UserConstant;
import com.yupi.usercenter.mapper.UserMapper;
import com.yupi.usercenter.model.User;
import com.yupi.usercenter.model.response.CommonResponse;
import com.yupi.usercenter.model.response.LoginUserRsp;
import com.yupi.usercenter.model.response.RegisterUserRsp;
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
 * @author lipeng
 * {@code @description} 针对表【user(用户表)】的数据库操作Service实现
 * {@code @createDate} 2024-10-21 18:42:42
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    private static final int USERNAME_MIN_LENGTH = 4;
    private static final int USERNAME_MAX_LENGTH = 256;
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int PASSWORD_MAX_LENGTH = 2048;

    public static final String SUFFIX_SALT = "suARnTClqnWOx8";
    private static final String USER_LOGIN_INFO = "user_login_info";

    public RegisterUserRsp userRegister(@NonNull String userName, @NonNull String userPassword, @NonNull String repeatPassword) {
        RegisterUserRsp rspResult = new RegisterUserRsp();
        if (StringUtils.isAnyBlank(userName, userPassword, repeatPassword)) {
            rspResult.setMsg("用户名或密码不能为空");
            return rspResult;
        }
        // 用户名长度校验
        if (userName.length() < USERNAME_MIN_LENGTH || userName.length() > USERNAME_MAX_LENGTH) {
            rspResult.setMsg("用户名长度异常");
            return rspResult;
        }
        // 用户名特殊字符校验
        String regex = "[!@#$%^&*()_+\\-={}\\[\\]:\";',.?/\\\\|]"; /*随便搜的regex */
        Matcher matcher = Pattern.compile(regex).matcher(userName);
        if (matcher.find()) {
            rspResult.setMsg("用户名不能包含特殊字符");
            return rspResult;
        }
        // 密码长度校验
        if (userPassword.length() < PASSWORD_MIN_LENGTH || userPassword.length() > PASSWORD_MAX_LENGTH) {
            rspResult.setMsg("密码长度异常");
            return rspResult;
        }
        // 密码确认校验
        if (!userPassword.equals(repeatPassword)) {
            rspResult.setMsg("两次输入的密码不一致");
            return rspResult;
        }
        // userName 唯一
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        long count = this.count(queryWrapper.eq("user_name", userName));
        if (count > 0) {
            rspResult.setMsg("该用户名已存在");
            return rspResult;
        }
        // 插入一个新用户，返回用户id
        String userPasswordMd5 = DigestUtils.md5DigestAsHex((userPassword + SUFFIX_SALT).getBytes());
        User newUser = new User(userName, userPasswordMd5);
        this.save(newUser);
        return new RegisterUserRsp(newUser.getId(), "注册成功");
    }

    public LoginUserRsp userLogin(@NonNull String userName, @NonNull String userPassword, HttpServletRequest request) {
        LoginUserRsp rspResult = new LoginUserRsp(null, "");
        if (StringUtils.isAnyBlank(userName, userPassword)) {
            rspResult.setMsg("用户名或密码不能为空");
            return rspResult;
        }
        // 用户名长度校验
        if (userName.length() < USERNAME_MIN_LENGTH || userName.length() > USERNAME_MAX_LENGTH) {
            rspResult.setMsg("用户名长度异常");
            return rspResult;
        }
        // 用户名特殊字符校验
        String regex = "[!@#$%^&*()_+\\-={}\\[\\]:\";',.?/\\\\|]"; /*随便搜的regex */
        Matcher matcher = Pattern.compile(regex).matcher(userName);
        if (matcher.find()) {
            rspResult.setMsg("用户名不能包含特殊字符");
            return rspResult;
        }
        // 密码长度校验
        if (userPassword.length() < PASSWORD_MIN_LENGTH || userPassword.length() > PASSWORD_MAX_LENGTH) {
            rspResult.setMsg("密码长度异常");
            return rspResult;
        }
        // 密码校验
        String needCheckPasswordMD5 = DigestUtils.md5DigestAsHex((userPassword + SUFFIX_SALT).getBytes());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", userName);
        User savedUser = this.getOne(queryWrapper);
        if (savedUser == null) {
            rspResult.setMsg("该用户不存在");
            return rspResult;
        }
        String savedUserPasswordMD5 = savedUser.getUserPassword();
        if (needCheckPasswordMD5.equals(savedUserPasswordMD5)) {
            rspResult.setMsg("登录成功");
            User safeUser = getSafeUser(savedUser);
            rspResult.setUser(safeUser);
            request.getSession().setAttribute(USER_LOGIN_INFO, safeUser);
        } else {
            rspResult.setMsg("密码校验失败");
        }
        return rspResult;
    }

    @Override
    public CommonResponse searchUser(@NotNull String userName, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return new CommonResponse(-1, "无权限查询用户", null);
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<User>().like("user_name", userName);
        List<User> originalUserList = this.list(userQueryWrapper);
        List<User> safetyUserList = originalUserList.stream().map(this::getSafeUser).collect(Collectors.toList());
        return new CommonResponse(0, "查询成功", safetyUserList);
    }

    @Override
    public CommonResponse deleteUser(@NotNull Long userId, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return new CommonResponse(-1, "无权限删除用户", null);
        }
        boolean result = this.removeById(userId);
        return new CommonResponse(result ? 0 : -1, result ? "删除成功" : "删除失败", result);
    }

    @Override
    public @Nullable User currentUser(HttpServletRequest request) {
        Object userLoginInfo = request.getSession().getAttribute(USER_LOGIN_INFO);
        if (!(userLoginInfo instanceof User)) {
            return null;
        }
        User user = this.getById(((User)(userLoginInfo)).getId());
        if (user == null) {
            return null;
        }
        request.getSession().setAttribute(USER_LOGIN_INFO, user);
        return getSafeUser(user);
    }

    @Override
    public CommonResponse searchAllUser(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return new CommonResponse(-1, "无权限查询用户", null);
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        List<User> originalUserList = this.list(userQueryWrapper);
        List<User> safetyUserList = originalUserList.stream().map(this::getSafeUser).collect(Collectors.toList());
        return new CommonResponse(0, "查询成功", safetyUserList);
    }

    private boolean isAdmin(HttpServletRequest request) {
        if (request == null || request.getSession() == null) {
            return false;
        }
        Object userLoginInfo = request.getSession().getAttribute(USER_LOGIN_INFO);
        if (!(userLoginInfo instanceof User)) {
            return false;
        }
        return ((User) userLoginInfo).getUserRole() == UserConstant.USER_ROLE_ADMIN;
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




