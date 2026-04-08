package backendlab.team4you.caserecord;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CaseRecordRepository extends JpaRepository<CaseRecord, Long> {

    Optional<CaseRecord> findByCaseNumber(String caseNumber);

    boolean existsByCaseNumber(String caseNumber);
}
