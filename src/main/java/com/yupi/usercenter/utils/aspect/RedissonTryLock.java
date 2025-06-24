package com.yupi.usercenter.utils.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * redisson 并发加锁
 * @author lipeng
 * @since 2025/5/21 15:58
*/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedissonTryLock {
    String key();
    long waitTime();
    long leaseTime();
    TimeUnit timeUnit();
}
