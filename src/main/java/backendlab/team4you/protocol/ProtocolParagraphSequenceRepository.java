package backendlab.team4you.protocol;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface ProtocolParagraphSequenceRepository
        extends JpaRepository<ProtocolParagraphSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ProtocolParagraphSequence> findByRegistryIdAndYear(Long registryId, Integer year);
}
