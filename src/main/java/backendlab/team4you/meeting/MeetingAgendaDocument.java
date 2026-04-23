package backendlab.team4you.meeting;

import backendlab.team4you.casefile.CaseFile;
import jakarta.persistence.*;

@Entity
@Table(
        name = "meeting_agenda_document",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_meeting_agenda_document_item_file", columnNames = {"agenda_item_id", "case_file_id"})
        }
)
public class MeetingAgendaDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agenda_item_id", nullable = false)
    private MeetingAgendaItem agendaItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_file_id", nullable = false)
    private CaseFile caseFile;

    protected MeetingAgendaDocument() {
    }

    public MeetingAgendaDocument(MeetingAgendaItem agendaItem, CaseFile caseFile) {
        this.agendaItem = agendaItem;
        this.caseFile = caseFile;
    }

    public Long getId() {
        return id;
    }

    public MeetingAgendaItem getAgendaItem() {
        return agendaItem;
    }

    public void setAgendaItem(MeetingAgendaItem agendaItem) {
        this.agendaItem = agendaItem;
    }

    public CaseFile getCaseFile() {
        return caseFile;
    }

    public void setCaseFile(CaseFile caseFile) {
        this.caseFile = caseFile;
    }
}
