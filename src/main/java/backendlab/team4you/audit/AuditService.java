package backendlab.team4you.audit;


import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Service
public class AuditService {

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
        log.setTimestamp(ZonedDateTime.now());
        log.setStatus(status);
                log.setEntityType(entityType);
                log.setEntityId(entityId);

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
                auditLog.setEntityId(Math.toIntExact(entityId));
                auditLog.setDetails(details);
                auditLog.setStatus(status);
                auditLog.setTimestamp(ZonedDateTime.now());

                auditLogRepository.save(auditLog);

                System.out.println(" Audit log saved " + action);

            } catch (Exception e) {
                System.out.println("Failed to save audit log " + e.getMessage());
            }
        }

    }
}
