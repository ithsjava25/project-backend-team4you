package backendlab.team4you.meeting;

import backendlab.team4you.casefile.CaseFile;
import backendlab.team4you.casefile.CaseFileRepository;
import backendlab.team4you.caserecord.CaseRecord;
import backendlab.team4you.caserecord.CaseRecordRepository;
import backendlab.team4you.exceptions.*;
import backendlab.team4you.registry.Registry;
import backendlab.team4you.registry.RegistryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private MeetingAgendaItemRepository meetingAgendaItemRepository;

    @Mock
    private MeetingAgendaDocumentRepository meetingAgendaDocumentRepository;

    @Mock
    private RegistryRepository registryRepository;

    @Mock
    private CaseRecordRepository caseRecordRepository;

    @Mock
    private CaseFileRepository caseFileRepository;

    @InjectMocks
    private MeetingService meetingService;

    private Registry registry;
    private Registry otherRegistry;
    private Meeting meeting;

    private CaseRecord caseRecord;
    private CaseRecord otherRegistryCaseRecord;

    private CaseFile caseFile;
    private CaseFile wrongCaseFile;

    @BeforeEach
    void setUp() {
        registry = new Registry("Kommunstyrelsen", "KS");
        setField(registry, "id", 1L);

        otherRegistry = new Registry("Byggnadsnämnden", "BN");
        setField(otherRegistry, "id", 2L);

        meeting = new Meeting(
                registry,
                "KS april",
                LocalDateTime.of(2026, 4, 30, 13, 0),
                LocalDateTime.of(2026, 4, 30, 15, 0),
                "Sessionssalen",
                MeetingStatus.PLANNED,
                "Anteckning"
        );
        setField(meeting, "id", 10L);

        caseRecord = mock(CaseRecord.class);
        otherRegistryCaseRecord = mock(CaseRecord.class);
        caseFile = mock(CaseFile.class);
        wrongCaseFile = mock(CaseFile.class);
    }

    @Test
    @DisplayName("createMeeting should save meeting when input is valid")
    void createMeeting_shouldSaveMeeting_whenInputIsValid() {
        LocalDateTime startsAt = LocalDateTime.of(2026, 5, 10, 13, 0);
        LocalDateTime endsAt = LocalDateTime.of(2026, 5, 10, 15, 0);

        when(registryRepository.findById(1L)).thenReturn(Optional.of(registry));
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Meeting result = meetingService.createMeeting(
                1L,
                "Kommunstyrelsen maj",
                startsAt,
                endsAt,
                "Sessionssalen",
                "Viktig anteckning"
        );

        ArgumentCaptor<Meeting> captor = ArgumentCaptor.forClass(Meeting.class);
        verify(meetingRepository).save(captor.capture());

        Meeting savedMeeting = captor.getValue();
        assertThat(savedMeeting.getRegistry()).isEqualTo(registry);
        assertThat(savedMeeting.getTitle()).isEqualTo("Kommunstyrelsen maj");
        assertThat(savedMeeting.getStartsAt()).isEqualTo(startsAt);
        assertThat(savedMeeting.getEndsAt()).isEqualTo(endsAt);
        assertThat(savedMeeting.getLocation()).isEqualTo("Sessionssalen");
        assertThat(savedMeeting.getNotes()).isEqualTo("Viktig anteckning");

        assertThat(result.getTitle()).isEqualTo("Kommunstyrelsen maj");
    }

    @Test
    @DisplayName("createMeeting should throw InvalidMeetingStateException when title is blank")
    void createMeeting_shouldThrowInvalidMeetingStateException_whenTitleIsBlank() {
        assertThatThrownBy(() -> meetingService.createMeeting(
                1L,
                "   ",
                LocalDateTime.of(2026, 5, 10, 13, 0),
                null,
                null,
                null
        ))
                .isInstanceOf(InvalidMeetingStateException.class)
                .hasMessage("Titel måste anges.");
    }

    @Test
    @DisplayName("createMeeting should throw InvalidMeetingStateException when end is before start")
    void createMeeting_shouldThrowInvalidMeetingStateException_whenEndIsBeforeStart() {
        assertThatThrownBy(() -> meetingService.createMeeting(
                1L,
                "Testmöte",
                LocalDateTime.of(2026, 5, 10, 15, 0),
                LocalDateTime.of(2026, 5, 10, 13, 0),
                null,
                null
        ))
                .isInstanceOf(InvalidMeetingStateException.class)
                .hasMessage("Sluttid kan inte vara före starttid.");
    }

    @Test
    @DisplayName("getMeetingById should throw MeetingNotFoundException when meeting does not exist")
    void getMeetingById_shouldThrowMeetingNotFoundException_whenMeetingDoesNotExist() {
        when(meetingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.getMeetingById(999L))
                .isInstanceOf(MeetingNotFoundException.class)
                .hasMessage("Sammanträdet hittades inte.");
    }

    @Test
    @DisplayName("updateMeeting should update existing meeting")
    void updateMeeting_shouldUpdateExistingMeeting() {
        when(meetingRepository.findById(10L)).thenReturn(Optional.of(meeting));
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Meeting updated = meetingService.updateMeeting(
                10L,
                "Nytt mötesnamn",
                LocalDateTime.of(2026, 5, 1, 9, 0),
                LocalDateTime.of(2026, 5, 1, 11, 0),
                "Nya salen",
                "Nya anteckningar",
                MeetingStatus.PREPARING
        );

        assertThat(updated.getTitle()).isEqualTo("Nytt mötesnamn");
        assertThat(updated.getStartsAt()).isEqualTo(LocalDateTime.of(2026, 5, 1, 9, 0));
        assertThat(updated.getEndsAt()).isEqualTo(LocalDateTime.of(2026, 5, 1, 11, 0));
        assertThat(updated.getLocation()).isEqualTo("Nya salen");
        assertThat(updated.getNotes()).isEqualTo("Nya anteckningar");
        assertThat(updated.getStatus()).isEqualTo(MeetingStatus.PREPARING);
    }

    @Test
    @DisplayName("addCaseRecordToMeeting should create agenda item with next order")
    void addCaseRecordToMeeting_shouldCreateAgendaItemWithNextOrder() {
        when(caseRecord.getRegistry()).thenReturn(registry);

        when(meetingRepository.findById(10L)).thenReturn(Optional.of(meeting));
        when(caseRecordRepository.findById(100L)).thenReturn(Optional.of(caseRecord));
        when(meetingAgendaItemRepository.existsByMeetingAndCaseRecord(meeting, caseRecord)).thenReturn(false);
        when(meetingAgendaItemRepository.countByMeeting(meeting)).thenReturn(2L);
        when(meetingAgendaItemRepository.save(any(MeetingAgendaItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MeetingAgendaItem result = meetingService.addCaseRecordToMeeting(10L, 100L);

        assertThat(result.getMeeting()).isEqualTo(meeting);
        assertThat(result.getCaseRecord()).isEqualTo(caseRecord);
        assertThat(result.getAgendaOrder()).isEqualTo(3);
    }

    @Test
    @DisplayName("addCaseRecordToMeeting should throw DuplicateMeetingAgendaItemException when case record already exists")
    void addCaseRecordToMeeting_shouldThrowDuplicateMeetingAgendaItemException_whenCaseRecordAlreadyExists() {
        when(caseRecord.getRegistry()).thenReturn(registry);

        when(meetingRepository.findById(10L)).thenReturn(Optional.of(meeting));
        when(caseRecordRepository.findById(100L)).thenReturn(Optional.of(caseRecord));
        when(meetingAgendaItemRepository.existsByMeetingAndCaseRecord(meeting, caseRecord)).thenReturn(true);

        assertThatThrownBy(() -> meetingService.addCaseRecordToMeeting(10L, 100L))
                .isInstanceOf(DuplicateMeetingAgendaItemException.class)
                .hasMessage("Ärendet är redan tillagt på sammanträdet.");
    }

    @Test
    @DisplayName("addCaseRecordToMeeting should throw InvalidMeetingStateException when case record belongs to different registry")
    void addCaseRecordToMeeting_shouldThrowInvalidMeetingStateException_whenCaseRecordBelongsToDifferentRegistry() {
        when(otherRegistryCaseRecord.getRegistry()).thenReturn(otherRegistry);

        when(meetingRepository.findById(10L)).thenReturn(Optional.of(meeting));
        when(caseRecordRepository.findById(200L)).thenReturn(Optional.of(otherRegistryCaseRecord));

        assertThatThrownBy(() -> meetingService.addCaseRecordToMeeting(10L, 200L))
                .isInstanceOf(InvalidMeetingStateException.class)
                .hasMessage("Ärendet tillhör inte samma organisation som sammanträdet.");
    }

    @Test
    @DisplayName("addDocumentToAgendaItem should add document when file belongs to same case record")
    void addDocumentToAgendaItem_shouldAddDocument_whenFileBelongsToSameCaseRecord() {
        MeetingAgendaItem agendaItem = new MeetingAgendaItem(meeting, caseRecord, 1, null);
        setField(agendaItem, "id", 50L);

        when(caseFile.getCaseRecord()).thenReturn(caseRecord);
        when(caseRecord.getId()).thenReturn(100L);

        when(meetingRepository.findById(10L)).thenReturn(Optional.of(meeting));
        when(meetingAgendaItemRepository.findById(50L)).thenReturn(Optional.of(agendaItem));
        when(caseFileRepository.findByIdAndCaseRecordId(1000L, 100L))
                .thenReturn(Optional.of(caseFile));
        when(meetingAgendaDocumentRepository.existsByAgendaItemAndCaseFile(agendaItem, caseFile)).thenReturn(false);
        when(meetingAgendaDocumentRepository.save(any(MeetingAgendaDocument.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MeetingAgendaDocument result = meetingService.addDocumentToAgendaItem(10L, 50L, 1000L);

        assertThat(result.getAgendaItem()).isEqualTo(agendaItem);
        assertThat(result.getCaseFile()).isEqualTo(caseFile);
    }

    @Test
    @DisplayName("addDocumentToAgendaItem should throw DuplicateMeetingAgendaDocumentException when document already exists")
    void addDocumentToAgendaItem_shouldThrowDuplicateMeetingAgendaDocumentException_whenDocumentAlreadyExists() {
        MeetingAgendaItem agendaItem = new MeetingAgendaItem(meeting, caseRecord, 1, null);
        setField(agendaItem, "id", 50L);

        when(caseFile.getCaseRecord()).thenReturn(caseRecord);
        when(caseRecord.getId()).thenReturn(100L);

        when(meetingRepository.findById(10L)).thenReturn(Optional.of(meeting));
        when(meetingAgendaItemRepository.findById(50L)).thenReturn(Optional.of(agendaItem));
        when(caseFileRepository.findByIdAndCaseRecordId(1000L, 100L))
                .thenReturn(Optional.of(caseFile));
        when(meetingAgendaDocumentRepository.existsByAgendaItemAndCaseFile(agendaItem, caseFile)).thenReturn(true);

        assertThatThrownBy(() -> meetingService.addDocumentToAgendaItem(10L, 50L, 1000L))
                .isInstanceOf(DuplicateMeetingAgendaDocumentException.class)
                .hasMessage("Handlingen är redan vald för denna dagordningspunkt.");
    }

    @Test
    @DisplayName("addDocumentToAgendaItem should throw CaseFileNotFoundException when file does not belong to agenda item's case record")
    void addDocumentToAgendaItem_shouldThrowCaseFileNotFoundException_whenFileBelongsToWrongCaseRecord() {
        MeetingAgendaItem agendaItem = new MeetingAgendaItem(meeting, caseRecord, 1, null);
        setField(agendaItem, "id", 50L);

        when(caseRecord.getId()).thenReturn(100L);
        when(meetingRepository.findById(10L)).thenReturn(Optional.of(meeting));
        when(meetingAgendaItemRepository.findById(50L)).thenReturn(Optional.of(agendaItem));

        assertThatThrownBy(() -> meetingService.addDocumentToAgendaItem(10L, 50L, 2000L))
                .isInstanceOf(CaseFileNotFoundException.class)
                .hasMessage("File not found for case record. caseRecordId=100, fileId=2000");
    }

    @Test
    @DisplayName("removeAgendaItem should delete item and resequence remaining items")
    void removeAgendaItem_shouldDeleteItemAndResequenceRemainingItems() {
        MeetingAgendaItem item1 = new MeetingAgendaItem(meeting, caseRecord, 1, null);
        setField(item1, "id", 11L);

        MeetingAgendaItem item2 = new MeetingAgendaItem(meeting, caseRecord, 2, null);
        setField(item2, "id", 12L);

        MeetingAgendaItem item3 = new MeetingAgendaItem(meeting, caseRecord, 3, null);
        setField(item3, "id", 13L);

        when(meetingRepository.findById(10L)).thenReturn(Optional.of(meeting));
        when(meetingAgendaItemRepository.findById(12L)).thenReturn(Optional.of(item2));
        when(meetingAgendaItemRepository.findByMeetingOrderByAgendaOrderAsc(meeting))
                .thenReturn(List.of(item1, item3));

        meetingService.removeAgendaItem(10L, 12L);

        verify(meetingAgendaItemRepository).delete(item2);
        assertThat(item1.getAgendaOrder()).isEqualTo(1);
        assertThat(item3.getAgendaOrder()).isEqualTo(2);
    }

    @Test
    @DisplayName("moveAgendaItemUp should swap agenda order with previous item")
    void moveAgendaItemUp_shouldSwapAgendaOrderWithPreviousItem() {
        MeetingAgendaItem item1 = new MeetingAgendaItem(meeting, caseRecord, 1, null);
        setField(item1, "id", 11L);

        MeetingAgendaItem item2 = new MeetingAgendaItem(meeting, caseRecord, 2, null);
        setField(item2, "id", 12L);

        when(meetingRepository.findById(10L)).thenReturn(Optional.of(meeting));
        when(meetingAgendaItemRepository.findByIdAndMeeting(12L, meeting)).thenReturn(Optional.of(item2));
        when(meetingAgendaItemRepository.findByMeetingAndAgendaOrder(meeting, 1)).thenReturn(Optional.of(item1));

        meetingService.moveAgendaItemUp(10L, 12L);

        assertThat(item2.getAgendaOrder()).isEqualTo(1);
        assertThat(item1.getAgendaOrder()).isEqualTo(2);
        verify(meetingAgendaItemRepository, atLeastOnce()).saveAndFlush(any(MeetingAgendaItem.class));
    }

    @Test
    @DisplayName("moveAgendaItemDown should swap agenda order with next item")
    void moveAgendaItemDown_shouldSwapAgendaOrderWithNextItem() {
        MeetingAgendaItem item2 = new MeetingAgendaItem(meeting, caseRecord, 2, null);
        setField(item2, "id", 12L);

        MeetingAgendaItem item3 = new MeetingAgendaItem(meeting, caseRecord, 3, null);
        setField(item3, "id", 13L);

        when(meetingRepository.findById(10L)).thenReturn(Optional.of(meeting));
        when(meetingAgendaItemRepository.findByIdAndMeeting(12L, meeting)).thenReturn(Optional.of(item2));
        when(meetingAgendaItemRepository.findByMeetingAndAgendaOrder(meeting, 3)).thenReturn(Optional.of(item3));

        meetingService.moveAgendaItemDown(10L, 12L);

        assertThat(item2.getAgendaOrder()).isEqualTo(3);
        assertThat(item3.getAgendaOrder()).isEqualTo(2);
        verify(meetingAgendaItemRepository, atLeastOnce()).saveAndFlush(any(MeetingAgendaItem.class));
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
