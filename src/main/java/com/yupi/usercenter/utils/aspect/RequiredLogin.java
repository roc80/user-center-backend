package com.yupi.usercenter.utils.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 某些方法需要用户已登录才能继续执行
 * @author lipeng
 * @since 2025/5/23 11:05
*/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiredLogin {
}
