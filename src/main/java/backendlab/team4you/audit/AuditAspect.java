package backendlab.team4you.audit;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuditAspect {

    private final AuditService auditService;

    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }


    @AfterReturning(pointcut = "@annotation(auditAction)", returning = "result")
    public void logAudit(JoinPoint joinPoint, AuditAction auditAction, Object result) {
        try {

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = (auth != null) ? auth.getName() : "system";


            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            String ip = "unknown";
            String endpoint = "unknown";
            if (attrs != null) {
                ip = attrs.getRequest().getRemoteAddr();
                endpoint = attrs.getRequest().getRequestURI();
            }


            auditService.saveLog(
                    username,
                    null,
                    auditAction.action(),
                    endpoint,
                    "POST",
                    ip,
                    "SUCCESS",
                    auditAction.entity(),
                    0
            );

        } catch (Exception e) {

        }
    }
}