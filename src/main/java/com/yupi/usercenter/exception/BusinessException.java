package com.yupi.usercenter.exception;

import com.yupi.usercenter.model.base.Error;
import com.yupi.usercenter.model.base.ResponseUtils;
import lombok.Getter;

/**
 * 对于某些业务上异常的情况，抛出此异常，并记录到日志。
 *
 * @author lipeng
 * @since 2025/4/20 12:56
 */
@Getter
public class BusinessException extends RuntimeException {
    /**
     * @see com.yupi.usercenter.model.base.BaseResponse# code
     */
    int code;
    /**
     * @see com.yupi.usercenter.model.base.BaseResponse# description
     */
    String description;

    String dateTime;

    public BusinessException(int code, String message, String description) {
        super(message);
        this.code = code;
        this.description = description;
        this.dateTime = ResponseUtils.INSTANCE.getFormatedLocalDateTime();
    }

    public BusinessException(Error error, String description) {
        super(error.getMessage());
        this.code = error.getCode();
        this.description = description;
        this.dateTime = ResponseUtils.INSTANCE.getFormatedLocalDateTime();
    }
}
