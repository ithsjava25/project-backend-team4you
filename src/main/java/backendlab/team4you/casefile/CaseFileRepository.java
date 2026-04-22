package backendlab.team4you.casefile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CaseFileRepository extends JpaRepository<CaseFile, Long> {
    List<CaseFile> findByCaseRecordId(Long caseRecordId);
    Optional<CaseFile> findByIdAndCaseRecordId(Long id, Long caseRecordId);
}
