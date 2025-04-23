package com.yupi.usercenter.exception;

import com.yupi.usercenter.model.base.BaseResponse;
import com.yupi.usercenter.model.base.Error;
import com.yupi.usercenter.model.base.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse handleBusinessException(BusinessException e) {
        log.warn("BusinessException: " + e.getMessage(), e);
        return ResponseUtils.error(e.getCode(), e.getMessage(), e.getDescription(), e.getDateTime());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException: " + e.getMessage(), e);
        return ResponseUtils.error(Error.SERVER_ERROR);
    }

}
