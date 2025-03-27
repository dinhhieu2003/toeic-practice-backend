package com.toeic.toeic_practice_backend.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);
	
	@Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {}
	
	@Around("controllerMethods()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String methodName = joinPoint.getSignature().getName();
        long start = System.currentTimeMillis();
        
        log.info("User '{}' with role {} is calling method '{}'", username, authentication.getAuthorities(), methodName);

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info("User '{}' successfully executed method '{}' in {} ms", username, methodName, duration);
            return result;
        } catch (Exception e) {
            log.error("User '{}' encountered an error in method '{}': {}", username, methodName, e.getMessage());
            throw e;
        }
    }
}
