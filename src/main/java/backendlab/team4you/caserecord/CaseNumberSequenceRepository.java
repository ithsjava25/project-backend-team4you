package backendlab.team4you.caserecord;

import backendlab.team4you.registry.Registry;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface CaseNumberSequenceRepository extends JpaRepository<CaseNumberSequence, Long> {

    Optional<CaseNumberSequence> findByRegistryAndYear(Registry registry, Integer year);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CaseNumberSequence> findWithLockByRegistryAndYear(Registry registry, Integer year);
}
