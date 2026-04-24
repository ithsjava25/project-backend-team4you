package backendlab.team4you.protocol;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProtocolRepository extends JpaRepository<Protocol, Long> {

    boolean existsByMeetingId(Long meetingId);

    Optional<Protocol> findByMeetingId(Long meetingId);
}
