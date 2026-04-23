package backendlab.team4you.meeting;

import backendlab.team4you.caserecord.CaseRecord;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "meeting_agenda_item",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_meeting_agenda_item_meeting_case", columnNames = {"meeting_id", "case_record_id"}),
                @UniqueConstraint(name = "uk_meeting_agenda_item_meeting_order", columnNames = {"meeting_id", "agenda_order"})
        }
)
public class MeetingAgendaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_record_id", nullable = false)
    private CaseRecord caseRecord;

    @Column(name = "agenda_order", nullable = false)
    private Integer agendaOrder;

    @Column(name = "agenda_note", length = 1000)
    private String agendaNote;

    @OneToMany(mappedBy = "agendaItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeetingAgendaDocument> documents = new ArrayList<>();

    protected MeetingAgendaItem() {
    }

    public MeetingAgendaItem(
            Meeting meeting,
            CaseRecord caseRecord,
            Integer agendaOrder,
            String agendaNote
    ) {
        this.meeting = meeting;
        this.caseRecord = caseRecord;
        this.agendaOrder = agendaOrder;
        this.agendaNote = agendaNote;
    }

    public Long getId() {
        return id;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    public void setMeeting(Meeting meeting) {
        this.meeting = meeting;
    }

    public CaseRecord getCaseRecord() {
        return caseRecord;
    }

    public void setCaseRecord(CaseRecord caseRecord) {
        this.caseRecord = caseRecord;
    }

    public Integer getAgendaOrder() {
        return agendaOrder;
    }

    public void setAgendaOrder(Integer agendaOrder) {
        this.agendaOrder = agendaOrder;
    }

    public String getAgendaNote() {
        return agendaNote;
    }

    public void setAgendaNote(String agendaNote) {
        this.agendaNote = agendaNote;
    }

    public List<MeetingAgendaDocument> getDocuments() {
        return documents;
    }
}
