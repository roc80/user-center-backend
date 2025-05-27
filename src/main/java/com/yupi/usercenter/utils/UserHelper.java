package com.yupi.usercenter.utils;

import com.yupi.usercenter.constant.UserConstant;
import com.yupi.usercenter.exception.BusinessException;
import com.yupi.usercenter.model.base.Error;
import com.yupi.usercenter.model.dto.UserDTO;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author lipeng
 * @since 2025/5/23 14:19
*/
public class UserHelper {

    /**
     * @return Login UserDTO, 非空
     * 如果拿不到合法的userId，则会抛出自定义异常。
    */
    @NotNull
    public static UserDTO getUserDtoFromRequest(HttpServletRequest request) {
        if (request == null || request.getSession() == null) {
            throw new BusinessException(Error.CLIENT_NO_AUTH, "");
        }
        Object userLoginInfo = request.getSession().getAttribute(UserConstant.USER_LOGIN_INFO);
        if (!(userLoginInfo instanceof UserDTO)) {
            throw new BusinessException(Error.CLIENT_NO_AUTH, "");
        }
        UserDTO loginUser = (UserDTO) userLoginInfo;
        if (loginUser.getUserId() == null || loginUser.getUserId() <= 0) {
            throw new BusinessException(Error.CLIENT_PARAMS_ERROR, "");
        }
        return loginUser;
    }

    public static boolean isAdmin(UserDTO loginUser) {
        if (loginUser == null) {
            throw new BusinessException(Error.CLIENT_NO_AUTH, "");
        }
        return UserConstant.USER_ROLE_ADMIN.equals(loginUser.getUserRole());
    }

    public static boolean isUsrValid(UserDTO loginUser) {
        if (loginUser == null) {
            throw new BusinessException(Error.CLIENT_NO_AUTH, "");
        }
        return UserConstant.USER_STATE_NORMAL.equals(loginUser.getState());
    }
}
