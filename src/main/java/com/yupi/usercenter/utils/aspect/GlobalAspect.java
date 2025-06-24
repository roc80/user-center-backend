package com.yupi.usercenter.utils.aspect;

import com.yupi.usercenter.utils.UserHelper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.servlet.http.HttpServletRequest;

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

    private final RedissonClient redissonClient;

    private final HttpServletRequest request;

    private final SpelExpressionParser parser = new SpelExpressionParser();

    public GlobalAspect(RedissonClient redissonClient, HttpServletRequest request) {
        this.redissonClient = redissonClient;
        this.request = request;
    }

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

    @Around("@annotation(redissonTryLock)")
    public Object redissonTryLock(ProceedingJoinPoint joinPoint, RedissonTryLock redissonTryLock) throws Throwable {
        String lockKey = parseKey(redissonTryLock.key(), joinPoint);
        RLock rLock = redissonClient.getLock(lockKey);
        try {
            boolean hasAcquired = rLock.tryLock(
                    redissonTryLock.waitTime(),
                    redissonTryLock.leaseTime(),
                    redissonTryLock.timeUnit()
            );
            if (!hasAcquired) {
                return null;
            }
            return joinPoint.proceed();
        } catch (InterruptedException e) {
            log.error("redisson concurrency try lock throws a exception", e);
        } finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
        return null;
    }

    private String parseKey(String key, ProceedingJoinPoint joinPoint) {
        if (!key.contains("#{")) {
            return key;
        }

        // 获取方法参数名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        // 创建SpEL上下文
        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        // 解析表达式
        Expression expression = parser.parseExpression(key, new TemplateParserContext());
        return expression.getValue(context, String.class);
    }

    @Pointcut("@annotation(com.yupi.usercenter.utils.aspect.RequiredLogin)")
    public void requiredLoginPointCut() {
    }

    @Before("requiredLoginPointCut()")
    public void getUserLoginInfo() {
        UserHelper.getUserDtoFromRequest(request);
    }
}
