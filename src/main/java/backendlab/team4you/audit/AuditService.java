package backendlab.team4you.audit;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    AuditLogRepository auditLogRepository;
    public AuditService(AuditLogRepository auditRepository) {
        this.auditLogRepository = auditRepository;
    }

    public void saveLog( String username,
                                 String email,
                                 String action,
                                 String endpoint,
                                 String httpMethod,
                                 String ipAddress,
                                 String status,
                         String entityType,
                         int entityId) {

        AuditLog log = new AuditLog();

        log.setUsername(username);
        log.setEmail(email);
        log.setAction(action);
        log.setEndpoint(endpoint);
        log.setIpAddress(ipAddress);
        log.setTimestamp(ZonedDateTime.now(ZoneId.of("Europe/Stockholm")));
        log.setStatus(status);
        log.setHttpMethod(httpMethod);
                log.setEntityType(entityType);
                log.setEntityId((long) entityId);

        auditLogRepository.save(log);


    }


    public void log(String username,
                    String action,
                    String entityType,
                    Long entityId,
                    String details,
                    String status) {

        {
            try {
                AuditLog auditLog = new AuditLog();
                auditLog.setUsername(username);
                auditLog.setAction(action);
                auditLog.setEntityType(entityType);
                auditLog.setEntityId((entityId));
                auditLog.setDetails(details);
                auditLog.setStatus(status);
                auditLog.setTimestamp(ZonedDateTime.now(ZoneId.of("Europe/Stockholm")));

                auditLogRepository.save(auditLog);

                log.info("Audit log saved: action={}, entity={}:{}", action, entityType, entityId);

            } catch (Exception e) {
                System.out.println("Failed to save audit log " + e.getMessage());
            }
        }

    }
}
