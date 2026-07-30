package com.hrms.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect      // marks this as an aspect — Spring will scan it
@Component   // Spring manages it as a bean
@Slf4j
public class LoggingAspect {

    // @Around — runs AROUND the target method
    // Pointcut: @annotation(LogExecutionTime) means "any method annotated with @LogExecutionTime"
    // ProceedingJoinPoint gives us control: we decide when to call proceed() (the real method)
    @Around("@annotation(LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className  = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        long start = System.currentTimeMillis();
        Object result;

        try {
            // This is where the real method executes
            // Everything before = "before advice"
            // Everything after = "after advice"
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            long duration = System.currentTimeMillis() - start;
            log.warn("[AOP] {}.{}() FAILED after {}ms — {}",
                    className, methodName, duration, throwable.getMessage());
            throw throwable;   // re-throw — we observe, not swallow
        }

        long duration = System.currentTimeMillis() - start;

        // Warn if a service method takes longer than 1 second
        // In Week 9 (Kubernetes) this feeds into health check thresholds
        if (duration > 1000) {
            log.warn("[AOP] SLOW: {}.{}() took {}ms", className, methodName, duration);
        } else {
            log.debug("[AOP] {}.{}() completed in {}ms", className, methodName, duration);
        }

        return result;
    }
}