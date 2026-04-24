package backendlab.team4you.caserecord;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CaseRecordRepository extends JpaRepository<CaseRecord, Long> {

    Optional<CaseRecord> findByCaseNumber(String caseNumber);
    List<CaseRecord> findByRegistryIdOrderByCreatedAtDesc(Long registryId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CaseRecord c where c.id = :id")
    Optional<CaseRecord> findByIdWithLock(Long id);
    boolean existsByCaseNumber(String caseNumber);

    @Query("SELECT c FROM CaseRecord c WHERE c.assignedUser.id = :officerId")
    Page<CaseRecord> findByAssignedUserId(String officerId, Pageable pageable);
}
