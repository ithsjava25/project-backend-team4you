package backendlab.team4you.casefile;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface CaseFileRepository extends JpaRepository<CaseFile, Long> {
    List<CaseFile> findByCaseRecordIdOrderByDocumentNumberAsc(Long caseRecordId);
    Optional<CaseFile> findByIdAndCaseRecordId(Long id, Long caseRecordId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CaseFile> findTopByCaseRecordIdOrderByDocumentNumberDesc(Long caseRecordId);
    List<CaseFile> findByCaseRecordIdOrderByUploadedAtDesc(Long caseRecordId);
}
