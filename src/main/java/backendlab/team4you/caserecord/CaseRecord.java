package backendlab.team4you.caserecord;

import backendlab.team4you.casefile.CaseFile;
import backendlab.team4you.registry.Registry;
import backendlab.team4you.user.UserEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private LocalDateTime openedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @OneToMany(mappedBy = "caseRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CaseFile> caseFiles = new ArrayList<>();

    public CaseRecord() {
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
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.caseNumber == null || this.caseNumber.isBlank()) {
            throw new IllegalStateException("caseNumber must be set before persisting");
        }
        this.createdAt = now;
        if (this.openedAt == null) {
            this.openedAt = now;
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

    public void setId(long l) {
        this.id = l;
    }

    public void setAssignedUser(UserEntity assignedUser) {
        this.assignedUser = assignedUser;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
