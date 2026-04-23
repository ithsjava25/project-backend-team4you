package backendlab.team4you.meeting;

import backendlab.team4you.registry.Registry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    List<Meeting> findByRegistryOrderByStartsAtAsc(Registry registry);
    List<Meeting> findByRegistryOrderByStartsAtDesc(Registry registry);
    List<Meeting> findAllByOrderByStartsAtDesc();
}
