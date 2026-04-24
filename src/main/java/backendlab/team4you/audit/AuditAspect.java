package backendlab.team4you.audit;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditService auditService;

    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    @Pointcut("within(backendlab.team4you..*)")
    public void controllerMethods() {}

    @AfterReturning(pointcut = "@annotation(auditAction)", returning = "result")
    public void logAuditSuccess(JoinPoint joinPoint, AuditAction auditAction, Object result) {
        record(joinPoint, auditAction, "SUCCESS");
    }

    @AfterThrowing(pointcut = "@annotation(auditAction)", throwing = "ex")
    public void logAuditFailure(JoinPoint joinPoint, AuditAction auditAction, Throwable ex) {
        record(joinPoint, auditAction, "FAILURE");
    }

    private void record(JoinPoint joinPoint, AuditAction auditAction, String status) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = (auth != null) ? auth.getName() : "anonymous";

            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            String ip = "unknown";
            String endpoint = "unknown";
            String httpMethod = "UNKNOWN";

            if (attrs != null) {
                ip = attrs.getRequest().getRemoteAddr();
                endpoint = attrs.getRequest().getRequestURI();
                httpMethod = attrs.getRequest().getMethod();
            }


            int entityId = 0;
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                if (arg instanceof Long) {
                    entityId = ((Long) arg).intValue();
                    break;
                }
            }

            auditService.saveLog(
                    username,
                    null,
                    auditAction.action(),
                    endpoint,
                    httpMethod,
                    ip,
                    status,
                    auditAction.entity(),
                    entityId
            );

        } catch (Exception e) {
            log.warn("Failed to persist audit log for {}: {}",
                    joinPoint.getSignature().toShortString(), e.getMessage(), e);
        }
    }
}