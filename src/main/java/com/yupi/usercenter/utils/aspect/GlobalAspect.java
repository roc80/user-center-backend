package com.yupi.usercenter.utils.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/**
 * 全局公共切面类
 *
 * @author lipeng
 * @since 2025/5/19 15:59
 */
@Component
@Aspect
public class GlobalAspect {

    @Around("@annotation(com.yupi.usercenter.utils.aspect.CalcExecutionTime)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object result = joinPoint.proceed();

        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        stopWatch.stop();
        System.out.println(className + "#" + methodName + "()共耗时: " + stopWatch.getTotalTimeSeconds() + "s");
        return result;
    }
}
