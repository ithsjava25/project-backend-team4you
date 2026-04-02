package backendlab.team4you.caserecord;

import backendlab.team4you.registry.Registry;
import backendlab.team4you.webauthn.UserEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "case_record",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_case_record_case_number", columnNames = "case_number")
        }
)
public class CaseRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_number", nullable = false, unique = true, length = 50)
    private String caseNumber;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "registry_id", nullable = false)
    private Registry registry;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false, length = 50)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private UserEntity owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id", nullable = false)
    private UserEntity assignedUser;

    @Column(name = "confidentiality_level", nullable = false, length = 50)
    private String confidentialityLevel = "OPEN";

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    protected CaseRecord() {
    }

    public CaseRecord(
            Registry registry,
            String title,
            String description,
            String status,
            UserEntity owner,
            UserEntity assignedUser,
            String confidentialityLevel,
            LocalDateTime openedAt
    ) {
        this.registry = registry;
        this.title = title;
        this.description = description;
        this.status = status;
        this.owner = owner;
        this.assignedUser = assignedUser;
        this.confidentialityLevel = confidentialityLevel;
        this.openedAt = openedAt;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        if (this.openedAt == null) {
            this.openedAt = now;
        }
        if (this.status == null) {
            this.status = "OPEN";
        }
        if (this.confidentialityLevel == null) {
            this.confidentialityLevel = "OPEN";
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public Registry getRegistry() {
        return registry;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public UserEntity getOwner() {
        return owner;
    }

    public UserEntity getAssignedUser() {
        return assignedUser;
    }

    public String getConfidentialityLevel() {
        return confidentialityLevel;
    }

    public LocalDateTime getOpenedAt() {
        return openedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }
}
