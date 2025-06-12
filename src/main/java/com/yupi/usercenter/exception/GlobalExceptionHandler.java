package com.yupi.usercenter.exception;

import com.yupi.usercenter.model.base.BaseResponse;
import com.yupi.usercenter.model.base.Error;
import com.yupi.usercenter.model.base.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<String> handleBusinessException(BusinessException e) {
        log.warn("BusinessException: message={}, description={}", e.getMessage(), e.description, e);
        return ResponseUtils.error(e.getCode(), e.getMessage(), e.getDescription(), e.getDateTime());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<String> handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException: {}", e.getMessage(), e);
        return ResponseUtils.error(Error.SERVER_ERROR);
    }

    /**
     * Controller接收参数时，反序列化错误
    */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public BaseResponse<String> handleHttpMessageException(HttpMessageNotReadableException e) {
        log.error("HttpMessageNotReadableException: {}", e.getMessage());
        return ResponseUtils.error(Error.CLIENT_PARAMS_ERROR, "参数格式错误");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public BaseResponse<String> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("MethodArgumentTypeMismatchException: {}", e.getMessage());
        return ResponseUtils.error(Error.CLIENT_PARAMS_ERROR, "");
    }

}
