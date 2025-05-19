package com.yupi.usercenter.utils.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 统计方法执行时间
 * @author lipeng
 * @since 2025/5/19 15:51
*/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CalcExecutionTime {
    String value() default "";
}
