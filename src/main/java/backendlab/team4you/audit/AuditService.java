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
                                 String status) {

        AuditLog log = new AuditLog();

        log.setUsername(username);
        log.setEmail(email);
        log.setAction(action);
        log.setEndpoint(endpoint);
        log.setHttpMethod(httpMethod);
        log.setIpAddress(ipAddress);
        log.setTimestamp(ZonedDateTime.now());
        log.setStatus(status);

        auditLogRepository.save(log);
    }


}
