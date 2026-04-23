package backendlab.team4you.audit;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.ZonedDateTime;

@Component
public class AuditAspect implements HandlerInterceptor {

    private static final Logger logger =
            LoggerFactory.getLogger(AuditAspect.class);

    private final AuditLogRepository auditRepository;

    public AuditAspect(AuditLogRepository auditLogRepository, AuditLogRepository auditRepository) {
        this.auditRepository = auditLogRepository;

    }

    @Pointcut("within(backendlab.team4you.controller..*)")
    public void controllerMethods() {}

    @AfterReturning("controllerMethods()")
    public void logAfter(JoinPoint joinPoint) {
        try {
            String methodName = joinPoint.getSignature().getName();

            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            String username = authentication != null
                    ? authentication.getName()
                    : "anonymous";

            AuditLog log = new AuditLog();
            log.setAction(methodName.toUpperCase());
            log.setUsername(username);
            log.setTimestamp(ZonedDateTime.now());
            log.setDetails("Executed method: " + methodName);

            auditRepository.save(log);

            logger.info("Audit log saved for method: {}", methodName);

        } catch (Exception e) {
            logger.error("Failed to save audit log", e);
        }
    }
}
