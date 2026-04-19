package backendlab.team4you.caserecord;

import backendlab.team4you.registry.Registry;
import backendlab.team4you.user.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

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

    @Column(name = "case_number", nullable = false, length = 50, updatable = false)
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

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private UserEntity owner;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id", nullable = false)
    private UserEntity assignedUser;

    @Column(name = "confidentiality_level", nullable = false, length = 50)
    private String confidentialityLevel = "OPEN";

    @Column(name = "opened_at", nullable = false)
    private ZonedDateTime openedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "closed_at")
    private ZonedDateTime closedAt;







    public CaseRecord(
            Registry registry,
            String title,
            String description,
            String status,
            UserEntity owner,
            UserEntity assignedUser,
            String confidentialityLevel,
            ZonedDateTime openedAt
    ) {
        this.registry = Objects.requireNonNull(registry, "registry is required");
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        this.title = title.trim();
        this.description = description;
        this.status = (status == null || status.isBlank()) ? "OPEN" : status.trim();
        this.owner = Objects.requireNonNull(owner, "owner is required");
        this.assignedUser = Objects.requireNonNull(assignedUser, "assignedUser is required");
        this.confidentialityLevel =
                (confidentialityLevel == null || confidentialityLevel.isBlank())
                        ? "OPEN"
                        : confidentialityLevel.trim();

        this.openedAt = openedAt;
        this.createdAt = ZonedDateTime.now(ZoneId.of("Europe/Stockholm"));
    }

    @PrePersist
    void onCreate() {
        ZonedDateTime now = ZonedDateTime.now();


        if (this.caseNumber == null || this.caseNumber.isBlank()) {
            throw new IllegalStateException("caseNumber must be set before persisting");
        }
        this.createdAt = now;
        if (this.openedAt == null) {
            this.openedAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }

    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = ZonedDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        if (this.caseNumber != null) {
            throw new IllegalStateException("caseNumber is immutable once set");
        }
        if (caseNumber == null || caseNumber.isBlank()) {
            throw new IllegalArgumentException("caseNumber is required");
        }
        if (caseNumber.length() > 50) {
            throw new IllegalArgumentException("caseNumber length must be <= 50");
        }
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
    public ZonedDateTime getOpenedAt() {
        return openedAt;
    }
    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }
    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }
    public ZonedDateTime getClosedAt() {
        return closedAt;
    }
    public void setClosedAt(ZonedDateTime closedAt) {
        this.closedAt = closedAt;
    }



}
