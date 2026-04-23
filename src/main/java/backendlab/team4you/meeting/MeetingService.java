package backendlab.team4you.meeting;

import backendlab.team4you.casefile.CaseFile;
import backendlab.team4you.casefile.CaseFileRepository;
import backendlab.team4you.caserecord.CaseRecord;
import backendlab.team4you.caserecord.CaseRecordRepository;
import backendlab.team4you.exceptions.*;
import backendlab.team4you.registry.Registry;
import backendlab.team4you.registry.RegistryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final MeetingAgendaItemRepository meetingAgendaItemRepository;
    private final MeetingAgendaDocumentRepository meetingAgendaDocumentRepository;
    private final RegistryRepository registryRepository;
    private final CaseRecordRepository caseRecordRepository;
    private final CaseFileRepository caseFileRepository;

    public MeetingService(
            MeetingRepository meetingRepository,
            MeetingAgendaItemRepository meetingAgendaItemRepository,
            MeetingAgendaDocumentRepository meetingAgendaDocumentRepository,
            RegistryRepository registryRepository,
            CaseRecordRepository caseRecordRepository,
            CaseFileRepository caseFileRepository
    ) {
        this.meetingRepository = meetingRepository;
        this.meetingAgendaItemRepository = meetingAgendaItemRepository;
        this.meetingAgendaDocumentRepository = meetingAgendaDocumentRepository;
        this.registryRepository = registryRepository;
        this.caseRecordRepository = caseRecordRepository;
        this.caseFileRepository = caseFileRepository;
    }

    public Meeting createMeeting(
            Long registryId,
            String title,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            String location,
            String notes
    ) {
        if (registryId == null) {
            throw new InvalidMeetingStateException("Registry-id måste anges.");
        }

        if (title == null || title.isBlank()) {
            throw new InvalidMeetingStateException("Titel måste anges.");
        }

        if (startsAt == null) {
            throw new InvalidMeetingStateException("Starttid måste anges.");
        }

        if (endsAt != null && endsAt.isBefore(startsAt)) {
            throw new InvalidMeetingStateException("Sluttid kan inte vara före starttid.");
        }

        Registry registry = registryRepository.findById(registryId)
                .orElseThrow(() -> new RegistryNotFoundException("Registry hittades inte."));

        Meeting meeting = new Meeting(
                registry,
                title.trim(),
                startsAt,
                endsAt,
                blankToNull(location),
                MeetingStatus.PLANNED,
                blankToNull(notes)
        );

        return meetingRepository.save(meeting);
    }

    @Transactional
    public Meeting updateMeeting(
            Long meetingId,
            String title,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            String location,
            String notes,
            MeetingStatus status
    ) {
        if (meetingId == null) {
            throw new InvalidMeetingStateException("Meeting-id måste anges.");
        }

        if (title == null || title.isBlank()) {
            throw new InvalidMeetingStateException("Titel måste anges.");
        }

        if (startsAt == null) {
            throw new InvalidMeetingStateException("Starttid måste anges.");
        }

        if (endsAt != null && endsAt.isBefore(startsAt)) {
            throw new InvalidMeetingStateException("Sluttid kan inte vara före starttid.");
        }

        if (status == null) {
            throw new InvalidMeetingStateException("Status måste anges.");
        }

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException("Sammanträdet hittades inte."));

        meeting.setTitle(title.trim());
        meeting.setStartsAt(startsAt);
        meeting.setEndsAt(endsAt);
        meeting.setLocation(blankToNull(location));
        meeting.setNotes(blankToNull(notes));
        meeting.setStatus(status);

        return meetingRepository.save(meeting);
    }

    @Transactional
    public void deleteMeeting(Long meetingId) {
        if (meetingId == null) {
            throw new InvalidMeetingStateException("Meeting-id måste anges.");
        }

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException("Sammanträdet hittades inte."));

        meetingRepository.delete(meeting);
    }

    @Transactional(readOnly = true)
    public List<Meeting> getMeetingsForRegistry(Long registryId) {
        Registry registry = registryRepository.findById(registryId)
                .orElseThrow(() -> new IllegalArgumentException("Registry hittades inte."));

        return meetingRepository.findByRegistryOrderByStartsAtDesc(registry);
    }

    @Transactional(readOnly = true)
    public Meeting getMeetingById(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException("Sammanträdet hittades inte."));
    }

    @Transactional(readOnly = true)
    public List<Meeting> getAllMeetings() {
        return meetingRepository.findAllByOrderByStartsAtDesc();
    }

    @Transactional(readOnly = true)
    public List<MeetingAgendaItem> getAgendaItems(Long meetingId) {
        Meeting meeting = getMeetingById(meetingId);
        return meetingAgendaItemRepository.findByMeetingOrderByAgendaOrderAsc(meeting);
    }

    public MeetingAgendaItem addCaseRecordToMeeting(Long meetingId, Long caseRecordId) {
        if (meetingId == null) {
            throw new InvalidMeetingStateException("Meeting-id måste anges.");
        }

        if (caseRecordId == null) {
            throw new InvalidMeetingStateException("Case record-id måste anges.");
        }

        Meeting meeting = getMeetingById(meetingId);

        CaseRecord caseRecord = caseRecordRepository.findById(caseRecordId)
                .orElseThrow(() -> new IllegalArgumentException("Ärendet hittades inte."));

        validateCaseRecordBelongsToMeetingRegistry(meeting, caseRecord);

        if (meetingAgendaItemRepository.existsByMeetingAndCaseRecord(meeting, caseRecord)) {
            throw new DuplicateMeetingAgendaItemException("Ärendet är redan tillagt på sammanträdet.");
        }

        long nextAgendaOrder = meetingAgendaItemRepository.countByMeeting(meeting) + 1;

        MeetingAgendaItem agendaItem = new MeetingAgendaItem(
                meeting,
                caseRecord,
                (int) nextAgendaOrder,
                null
        );

        return meetingAgendaItemRepository.save(agendaItem);
    }

    @Transactional
    public void moveAgendaItemUp(Long meetingId, Long agendaItemId) {
        Meeting meeting = getMeetingById(meetingId);

        MeetingAgendaItem currentItem = meetingAgendaItemRepository.findByIdAndMeeting(agendaItemId, meeting)
                .orElseThrow(() -> new MeetingAgendaItemNotFoundException("Dagordningspunkten hittades inte."));

        if (currentItem.getAgendaOrder() == null || currentItem.getAgendaOrder() <= 1) {
            return;
        }

        int currentOrder = currentItem.getAgendaOrder();
        int targetOrder = currentOrder - 1;

        MeetingAgendaItem previousItem = meetingAgendaItemRepository.findByMeetingAndAgendaOrder(meeting, targetOrder)
                .orElseThrow(() -> new InvalidMeetingStateException("Kunde inte flytta upp dagordningspunkten."));

        currentItem.setAgendaOrder(0);
        meetingAgendaItemRepository.saveAndFlush(currentItem);

        previousItem.setAgendaOrder(currentOrder);
        meetingAgendaItemRepository.saveAndFlush(previousItem);

        currentItem.setAgendaOrder(targetOrder);
        meetingAgendaItemRepository.saveAndFlush(currentItem);
    }

    @Transactional
    public void moveAgendaItemDown(Long meetingId, Long agendaItemId) {
        Meeting meeting = getMeetingById(meetingId);

        MeetingAgendaItem currentItem = meetingAgendaItemRepository.findByIdAndMeeting(agendaItemId, meeting)
                .orElseThrow(() -> new MeetingAgendaItemNotFoundException("Dagordningspunkten hittades inte."));

        if (currentItem.getAgendaOrder() == null) {
            return;
        }

        int currentOrder = currentItem.getAgendaOrder();
        int targetOrder = currentOrder + 1;

        MeetingAgendaItem nextItem = meetingAgendaItemRepository.findByMeetingAndAgendaOrder(meeting, targetOrder)
                .orElse(null);

        if (nextItem == null) {
            return;
        }

        currentItem.setAgendaOrder(0);
        meetingAgendaItemRepository.saveAndFlush(currentItem);

        nextItem.setAgendaOrder(currentOrder);
        meetingAgendaItemRepository.saveAndFlush(nextItem);

        currentItem.setAgendaOrder(targetOrder);
        meetingAgendaItemRepository.saveAndFlush(currentItem);
    }

    public void removeAgendaItem(Long meetingId, Long agendaItemId) {
        Meeting meeting = getMeetingById(meetingId);

        MeetingAgendaItem agendaItem = meetingAgendaItemRepository.findById(agendaItemId)
                .orElseThrow(() -> new MeetingAgendaItemNotFoundException("Dagordningspunkten hittades inte."));

        if (!agendaItem.getMeeting().getId().equals(meeting.getId())) {
            throw new InvalidMeetingStateException("Dagordningspunkten tillhör inte detta sammanträde.");
        }

        meetingAgendaItemRepository.delete(agendaItem);
        resequenceAgendaItems(meeting);
    }

    @Transactional(readOnly = true)
    public List<MeetingAgendaDocument> getAgendaDocuments(Long agendaItemId) {
        MeetingAgendaItem agendaItem = meetingAgendaItemRepository.findById(agendaItemId)
                .orElseThrow(() -> new MeetingAgendaItemNotFoundException("Dagordningspunkten hittades inte."));

        return meetingAgendaDocumentRepository.findByAgendaItem(agendaItem);
    }

    @Transactional(readOnly = true)
    public List<CaseFile> getAvailableCaseFilesForAgendaItem(Long agendaItemId) {
        MeetingAgendaItem agendaItem = meetingAgendaItemRepository.findById(agendaItemId)
                .orElseThrow(() -> new MeetingAgendaItemNotFoundException("Dagordningspunkten hittades inte."));

        return caseFileRepository.findByCaseRecordIdOrderByUploadedAtDesc(agendaItem.getCaseRecord().getId());
    }

    public MeetingAgendaDocument addDocumentToAgendaItem(Long meetingId, Long agendaItemId, Long caseFileId) {
        Meeting meeting = getMeetingById(meetingId);

        MeetingAgendaItem agendaItem = meetingAgendaItemRepository.findById(agendaItemId)
                .orElseThrow(() -> new MeetingAgendaItemNotFoundException("Dagordningspunkten hittades inte."));

        if (!agendaItem.getMeeting().getId().equals(meeting.getId())) {
            throw new InvalidMeetingStateException("Dagordningspunkten tillhör inte detta sammanträde.");
        }

        CaseFile caseFile = caseFileRepository.findById(caseFileId)
                .orElseThrow(() -> new MeetingAgendaDocumentNotFoundException("Handlingen hittades inte."));

        validateCaseFileBelongsToAgendaItemCaseRecord(agendaItem, caseFile);

        if (meetingAgendaDocumentRepository.existsByAgendaItemAndCaseFile(agendaItem, caseFile)) {
            throw new DuplicateMeetingAgendaDocumentException("Handlingen är redan vald för denna dagordningspunkt.");
        }

        MeetingAgendaDocument document = new MeetingAgendaDocument(agendaItem, caseFile);
        return meetingAgendaDocumentRepository.save(document);
    }

    public void removeDocumentFromAgendaItem(Long meetingId, Long agendaItemId, Long documentId) {
        Meeting meeting = getMeetingById(meetingId);

        MeetingAgendaItem agendaItem = meetingAgendaItemRepository.findById(agendaItemId)
                .orElseThrow(() -> new MeetingAgendaItemNotFoundException("Dagordningspunkten hittades inte."));

        if (!agendaItem.getMeeting().getId().equals(meeting.getId())) {
            throw new InvalidMeetingStateException("Dagordningspunkten tillhör inte detta sammanträde.");
        }

        MeetingAgendaDocument document = meetingAgendaDocumentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Dokumentkopplingen hittades inte."));

        if (!document.getAgendaItem().getId().equals(agendaItem.getId())) {
            throw new InvalidMeetingStateException("Dokumentet tillhör inte denna dagordningspunkt.");
        }

        meetingAgendaDocumentRepository.delete(document);
    }

    public Meeting updateMeetingStatus(Long meetingId, MeetingStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status måste anges.");
        }

        Meeting meeting = getMeetingById(meetingId);
        meeting.setStatus(status);

        return meetingRepository.save(meeting);
    }

    private void validateCaseRecordBelongsToMeetingRegistry(Meeting meeting, CaseRecord caseRecord) {
        if (caseRecord.getRegistry() == null || caseRecord.getRegistry().getId() == null) {
            throw new InvalidMeetingStateException("Ärendet saknar diarum.");
        }

        if (!caseRecord.getRegistry().getId().equals(meeting.getRegistry().getId())) {
            throw new InvalidMeetingStateException("Ärendet tillhör inte samma organisation som sammanträdet.");
        }
    }

    private void validateCaseFileBelongsToAgendaItemCaseRecord(MeetingAgendaItem agendaItem, CaseFile caseFile) {
        if (caseFile.getCaseRecord() == null || caseFile.getCaseRecord().getId() == null) {
            throw new InvalidMeetingStateException("Handlingen saknar kopplat ärende.");
        }

        if (!caseFile.getCaseRecord().getId().equals(agendaItem.getCaseRecord().getId())) {
            throw new InvalidMeetingStateException("Handlingen tillhör inte ärendet på denna dagordningspunkt.");
        }
    }

    private void resequenceAgendaItems(Meeting meeting) {
        List<MeetingAgendaItem> agendaItems = meetingAgendaItemRepository.findByMeetingOrderByAgendaOrderAsc(meeting);

        int order = 1;
        for (MeetingAgendaItem agendaItem : agendaItems) {
            agendaItem.setAgendaOrder(order);
            order++;
        }
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
