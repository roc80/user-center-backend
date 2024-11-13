package com.yupi.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.usercenter.mapper.UserMapper;
import com.yupi.usercenter.model.User;
import com.yupi.usercenter.model.response.LoginUserRsp;
import com.yupi.usercenter.model.response.RegisterUserRsp;
import com.yupi.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        RegisterUserRsp rspResult = new RegisterUserRsp("");
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
        return new RegisterUserRsp(newUser.getId().toString());
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
            User safeUser = new User(savedUser.getUserName(), "");
            safeUser.setId(savedUser.getId());
            safeUser.setAvatarUrl(savedUser.getAvatarUrl());
            safeUser.setGender(savedUser.getGender());
            safeUser.setPhone(savedUser.getPhone());
            safeUser.setEmail(savedUser.getEmail());
            safeUser.setCreateDatetime(savedUser.getCreateDatetime());
            safeUser.setIsValid(savedUser.getIsValid());
            rspResult.setUser(safeUser);
            request.getSession().setAttribute(USER_LOGIN_INFO, safeUser);
        } else {
            rspResult.setMsg("密码校验失败");
        }
        return rspResult;
    }

}




