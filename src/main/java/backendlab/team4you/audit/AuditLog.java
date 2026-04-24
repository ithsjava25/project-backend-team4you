package backendlab.team4you.audit;


import jakarta.persistence.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "audit")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String username;
    private String email;

    String details;

    private String action;

    private String httpMethod;

    private String endpoint;

    private String entityType;

    private Long entityId;

    @Column(name = "ip_address")
    private String ipAddress;


    private ZonedDateTime timestamp;

    private String status;


    public AuditLog() {
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public String getEndpoint() {
        return endpoint;
    }
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    public String getEntityType() {
        return entityType;
    }
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
    public Long getEntityId() {
        return entityId;
    }
    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getIpAddress() {
        return ipAddress;
    }
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    public ZonedDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getDetails() {
        return details;
    }
    public void setDetails(String details) {
        this.details = details;
    }
    public String getHttpMethod() {
        return httpMethod;
    }
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }
}
