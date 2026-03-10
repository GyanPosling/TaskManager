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
    private static final String EXECUTED_IN_MS_MESSAGE = "{} executed in {} ms";
    private static final String FAILED_AFTER_MS_MESSAGE = "{} failed after {} ms: {}";

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
            log.warn(FAILED_AFTER_MS_MESSAGE,
                    methodName,
                    calculateExecutionTimeMillis(startTime),
                    ex.getMessage());
            throw ex;
        }
    }

    private void logExecutionTime(String methodName, long executionTimeMillis) {
        if (executionTimeMillis >= VERY_SLOW_THRESHOLD_MS) {
            log.warn(EXECUTED_IN_MS_MESSAGE, methodName, executionTimeMillis);
            return;
        }
        if (executionTimeMillis >= SLOW_THRESHOLD_MS) {
            log.info(EXECUTED_IN_MS_MESSAGE, methodName, executionTimeMillis);
            return;
        }
        log.debug(EXECUTED_IN_MS_MESSAGE, methodName, executionTimeMillis);
    }

    private long calculateExecutionTimeMillis(long startTime) {
        return (System.nanoTime() - startTime) / 1_000_000;
    }
}
