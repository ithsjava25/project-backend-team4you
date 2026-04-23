package backendlab.team4you.meeting;

import backendlab.team4you.caserecord.CaseRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeetingAgendaItemRepository extends JpaRepository<MeetingAgendaItem, Long> {

    List<MeetingAgendaItem> findByMeetingOrderByAgendaOrderAsc(Meeting meeting);

    Optional<MeetingAgendaItem> findById(Meeting meeting, CaseRecord caseRecord);

    boolean existsByMeetingAndCaseRecord(Meeting meeting, CaseRecord caseRecord);

    long countByMeeting(Meeting meeting);

    Optional<MeetingAgendaItem> findByMeetingAndAgendaOrder(Meeting meeting, Integer agendaOrder);

    Optional<MeetingAgendaItem> findByIdAndMeeting(Long id, Meeting meeting);
}
