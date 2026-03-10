package com.bsuir.taskmanager.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ServiceExecutionTimeAspect {
    private static final long SLOW_THRESHOLD_MS = 500;
    private static final long VERY_SLOW_THRESHOLD_MS = 1_000;

    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void serviceMethods() {
    }

    @Around("serviceMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            logExecutionTime(methodName, calculateExecutionTimeMillis(startTime));
            return result;
        } catch (Throwable ex) {
            log.warn("{} failed after {} ms: {}",
                    methodName,
                    calculateExecutionTimeMillis(startTime),
                    ex.getMessage());
            throw ex;
        }
    }

    private void logExecutionTime(String methodName, long executionTimeMillis) {
        if (executionTimeMillis >= VERY_SLOW_THRESHOLD_MS) {
            log.warn("{} executed in {} ms", methodName, executionTimeMillis);
            return;
        }
        if (executionTimeMillis >= SLOW_THRESHOLD_MS) {
            log.info("{} executed in {} ms", methodName, executionTimeMillis);
            return;
        }
        log.debug("{} executed in {} ms", methodName, executionTimeMillis);
    }

    private long calculateExecutionTimeMillis(long startTime) {
        return (System.nanoTime() - startTime) / 1_000_000;
    }
}
