package backendlab.team4you.log;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Entity
@Table(name = "log_entry")
public class LogEntity {
    @Id
    private Long id;

    private String action;
    private String performedBy;
    private String details;

    private ZonedDateTime createdAt = ZonedDateTime.now();

    public LogEntity() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
    public String getAction() {
        return action;
    }
    public String getPerformedBy() {
        return performedBy;
    }
    public String getDetails() {
        return details;
    }
    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public void setAction(String action) {
        this.action = action;
    }
    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }
    public void setDetails(String details) {
        this.details = details;
    }

}
