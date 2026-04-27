package backendlab.team4you.protocol;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface ProtocolRepository extends JpaRepository<Protocol, Long> {

    boolean existsByMeetingId(Long meetingId);

    Optional<Protocol> findByMeetingId(Long meetingId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Protocol> findWithLockById(Long id);
}
