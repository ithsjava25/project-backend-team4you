package backendlab.team4you.meeting;

import backendlab.team4you.registry.Registry;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    List<Meeting> findByRegistryOrderByStartsAtAsc(Registry registry);
    List<Meeting> findByRegistryOrderByStartsAtDesc(Registry registry);
    List<Meeting> findAllByOrderByStartsAtDesc();

    @Query("""
            select m
            from Meeting m
            where m.status = backendlab.team4you.meeting.MeetingStatus.COMPLETED
            and not exists (
                select p.id
                from Protocol p
                where p.meeting = m
            )
            order by m.startsAt desc
            """)
    List<Meeting> findCompletedMeetingsWithoutProtocol();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Meeting m where m.id = :id")
    Optional<Meeting> findByIdWithLock(Long id);
}
