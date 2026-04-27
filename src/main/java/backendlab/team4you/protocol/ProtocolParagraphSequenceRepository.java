package backendlab.team4you.protocol;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProtocolParagraphSequenceRepository
        extends JpaRepository<ProtocolParagraphSequence, Long> {

    @Modifying
    @Query(value = """
        insert into protocol_paragraph_sequence (registry_id, sequence_year, last_value)
        values (:registryId, :year, 0)
        on conflict (registry_id, sequence_year) do nothing
        """, nativeQuery = true)
    void insertIfMissing(
            @Param("registryId") Long registryId,
            @Param("year") Integer year
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ProtocolParagraphSequence> findByRegistryIdAndYear(Long registryId, Integer year);
}
