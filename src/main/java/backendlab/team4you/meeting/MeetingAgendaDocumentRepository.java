package backendlab.team4you.meeting;

import backendlab.team4you.casefile.CaseFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeetingAgendaDocumentRepository extends JpaRepository<MeetingAgendaDocument, Long> {

    List<MeetingAgendaDocument> findByAgendaItem(MeetingAgendaItem agendaItem);

    Optional<MeetingAgendaDocument> findByAgendaItemAndCaseFile(MeetingAgendaItem agendaItem, CaseFile caseFile);

    boolean existsByAgendaItemAndCaseFile(MeetingAgendaItem agendaItem, CaseFile caseFile);

    boolean existsByCaseFileId(Long caseFileId);
}
