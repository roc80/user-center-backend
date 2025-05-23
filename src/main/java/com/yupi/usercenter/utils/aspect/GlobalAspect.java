package com.yupi.usercenter.utils.aspect;

import com.yupi.usercenter.constant.RedisConstant;
import com.yupi.usercenter.utils.UserHelper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * 全局公共切面类
 *
 * @author lipeng
 * @since 2025/5/19 15:59
 */
@Component
@Aspect
@Slf4j
public class GlobalAspect {

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    HttpServletRequest request;

    @Around("@annotation(com.yupi.usercenter.utils.aspect.CalcExecutionTime)")
    public Object calcExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object result = joinPoint.proceed();

        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        stopWatch.stop();
        System.out.println(className + "#" + methodName + "()共耗时: " + stopWatch.getTotalTimeSeconds() + "s");
        return result;
    }

    @Around("@annotation(com.yupi.usercenter.utils.aspect.RedissonTryLock)")
    public Object redissonTryLock(ProceedingJoinPoint joinPoint) throws Throwable {
        String lockKeyName = RedisConstant.PROJECT_NAME + ":" + RedisConstant.MODULE_REDIS + ":lock";
        RLock rLock = redissonClient.getLock(lockKeyName);
        try {
            boolean hasAcquired = rLock.tryLock(0L, -1L, TimeUnit.MILLISECONDS);
            if (!hasAcquired) {
                return null;
            }

            joinPoint.proceed();

        } catch (InterruptedException e) {
            log.error("redisson concurrency try lock throws a exception", e);
        } finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
        return null;
    }

    @Pointcut("@annotation(com.yupi.usercenter.utils.aspect.RequiredLogin)")
    public void requiredLoginPointCut() {}

    @Before("requiredLoginPointCut()")
    public void getUserLoginInfo() {
        UserHelper.getUserDtoFromRequest(request);
    }
}
