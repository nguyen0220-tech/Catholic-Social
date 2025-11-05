package com.catholic.ac.kr.catholicsocial.custom;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class Logging {

    @Around("execution(* com.catholic.ac.kr.catholicsocial.service.*.*(..))")
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        log.info("==> {}.{}() called",className,methodName);


        long startTime = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long endTime = System.currentTimeMillis();

        long runtime = endTime - startTime;

        log.info("<== {}.{}() executed in {}ms",className,methodName,runtime);

        return result;
    }
}
