package backendlab.team4you.casefile;

import backendlab.team4you.caserecord.CaseRecord;
import backendlab.team4you.user.UserEntity;
import jakarta.persistence.*;

@Entity
@Table(
        name = "case_file_access",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_case_file_access_case_user",
                        columnNames = {"case_record_id", "user_id"}
                )
        }
)
public class CaseFileAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "case_record_id", nullable = false)
    private CaseRecord caseRecord;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "can_view_confidential_files", nullable = false)
    private boolean canViewConfidentialFiles;

    public Long getId() {
        return id;
    }

    public CaseRecord getCaseRecord() {
        return caseRecord;
    }

    public void setCaseRecord(CaseRecord caseRecord) {
        this.caseRecord = caseRecord;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public boolean isCanViewConfidentialFiles() {
        return canViewConfidentialFiles;
    }

    public void setCanViewConfidentialFiles(boolean canViewConfidentialFiles) {
        this.canViewConfidentialFiles = canViewConfidentialFiles;
    }
}
