package ru.kelf54.starter.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class AuditAspect {
    @Around("@annotation(auditAnnotation)")
    public Object auditMethod(ProceedingJoinPoint pjp, WeylandWatchingYou auditAnnotation) throws Throwable {
        String methodName = pjp.getSignature().getName();
        Object[] args = pjp.getArgs();

        try {
            Object result = pjp.proceed();
            logAction(methodName, args, result, auditAnnotation.mode());
            return result;
        } catch (Throwable e) {
            logAction(methodName, args, e, auditAnnotation.mode());
            throw e;
        }
    }

    private void logAction(String method, Object[] args, Object result, String mode) {
        String logMessage = String.format(
                "Method: %s | Args: %s | Result: %s", method, Arrays.toString(args), result
        );

        if ("KAFKA".equals(mode)) {
            System.out.println("[KAFKA AUDIT] " + logMessage);
        } else {
            System.out.println("[CONSOLE AUDIT] " + logMessage);
        }
    }
}
