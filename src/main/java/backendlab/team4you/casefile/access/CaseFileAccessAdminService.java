package backendlab.team4you.casefile.access;

import backendlab.team4you.caserecord.CaseRecord;
import backendlab.team4you.caserecord.CaseRecordRepository;
import backendlab.team4you.exceptions.CaseRecordNotFoundException;
import backendlab.team4you.exceptions.UserNotFoundException;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CaseFileAccessAdminService {

    private final CaseFileAccessRepository caseFileAccessRepository;
    private final CaseRecordRepository caseRecordRepository;
    private final UserRepository userRepository;

    public CaseFileAccessAdminService(CaseFileAccessRepository caseFileAccessRepository,
                                      CaseRecordRepository caseRecordRepository,
                                      UserRepository userRepository) {
        this.caseFileAccessRepository = caseFileAccessRepository;
        this.caseRecordRepository = caseRecordRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void grantConfidentialFileAccess(Long caseRecordId, String userId) {
        CaseRecord caseRecord = caseRecordRepository.findById(caseRecordId)
                .orElseThrow(() -> new CaseRecordNotFoundException(caseRecordId));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        CaseFileAccess access = caseFileAccessRepository
                .findByCaseRecordIdAndUserId(caseRecordId, userId)
                .orElseGet(CaseFileAccess::new);

        access.setCaseRecord(caseRecord);
        access.setUser(user);
        access.setCanViewConfidentialFiles(true);

        caseFileAccessRepository.save(access);
    }

    @Transactional
    public void revokeConfidentialFileAccess(Long caseRecordId, String userId) {
        caseFileAccessRepository.findByCaseRecordIdAndUserId(caseRecordId, userId)
                .ifPresent(access -> {
                    access.setCanViewConfidentialFiles(false);
                    caseFileAccessRepository.save(access);
                });
    }
}
