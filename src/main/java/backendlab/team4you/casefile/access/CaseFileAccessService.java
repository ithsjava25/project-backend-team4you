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

        return hasConfidentialFileAccess(user, caseFile);
    }

    public boolean canDeleteFile(UserEntity user, CaseFile caseFile) {
        if (user == null) {
            return false;
        }

        if (isAdmin(user)) {
            return true;
        }

        return hasConfidentialFileAccess(user, caseFile);
    }

    public boolean canUploadFile(UserEntity user, Long caseRecordId, FileConfidentialityLevel confidentialityLevel) {
        if (user == null) {
            return false;
        }

        if (isAdmin(user)) {
            return true;
        }

        FileConfidentialityLevel effectiveLevel =
                confidentialityLevel != null ? confidentialityLevel : FileConfidentialityLevel.OPEN;

        if (effectiveLevel == FileConfidentialityLevel.OPEN) {
            return true;
        }

        return caseFileAccessRepository.existsByCaseRecordIdAndUserIdAndCanViewConfidentialFilesTrue(
                caseRecordId,
                user.getIdAsString()
        );
    }

    private boolean hasConfidentialFileAccess(UserEntity user, CaseFile caseFile) {
        return caseFileAccessRepository.existsByCaseRecordIdAndUserIdAndCanViewConfidentialFilesTrue(
                caseFile.getCaseRecord().getId(),
                user.getIdAsString()
        );
    }

    private boolean isAdmin(UserEntity user) {
        return user.getRole() == UserRole.ADMIN;
    }
}
