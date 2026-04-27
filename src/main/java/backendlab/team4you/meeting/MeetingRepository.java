package backendlab.team4you.meeting;

import backendlab.team4you.registry.Registry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

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
}
