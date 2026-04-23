package backendlab.team4you.casefile.access;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CaseFileAccessRepository extends JpaRepository<CaseFileAccess, Long> {

    boolean existsByCaseRecordIdAndUserIdAndCanViewConfidentialFilesTrue(Long caseRecordId, String userId);

    Optional<CaseFileAccess> findByCaseRecordIdAndUserId(Long caseRecordId, String userId);
}
