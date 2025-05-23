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

}
