package backendlab.team4you.casefile.access;

import backendlab.team4you.casefile.CaseFile;
import backendlab.team4you.casefile.FileConfidentialityLevel;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserRole;
import org.springframework.stereotype.Service;

@Service
public class CaseFileAccessService {

    private final CaseFileAccessRepository caseFileAccessRepository;

    public CaseFileAccessService(CaseFileAccessRepository caseFileAccessRepository) {
        this.caseFileAccessRepository = caseFileAccessRepository;
    }

    public boolean canViewFile(UserEntity user, CaseFile caseFile) {
        if (caseFile.getConfidentialityLevel() == FileConfidentialityLevel.OPEN) {
            return true;
        }

        if (user == null) {
            return false;
        }

        if (isAdmin(user)) {
            return true;
        }

        return caseFileAccessRepository.existsByCaseRecordIdAndUserIdAndCanViewConfidentialFilesTrue(
                caseFile.getCaseRecord().getId(),
                user.getIdAsString()
        );
    }

    private boolean isAdmin(UserEntity user) {
        return UserRole.ADMIN.name().equals(user.getRole());
    }
}
