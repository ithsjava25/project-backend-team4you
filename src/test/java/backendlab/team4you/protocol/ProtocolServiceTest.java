package backendlab.team4you.protocol;

import backendlab.team4you.casefile.CaseFile;
import backendlab.team4you.caserecord.CaseRecord;
import backendlab.team4you.exceptions.*;
import backendlab.team4you.meeting.Meeting;
import backendlab.team4you.meeting.MeetingAgendaItem;
import backendlab.team4you.meeting.MeetingRepository;
import backendlab.team4you.meeting.MeetingStatus;
import backendlab.team4you.registry.Registry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProtocolServiceTest {

    @Mock
    private ProtocolRepository protocolRepository;

    @Mock
    private ProtocolParagraphSequenceRepository sequenceRepository;

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private ProtocolParagraphRepository paragraphRepository;

    @InjectMocks
    private ProtocolService protocolService;

    private Registry registry;
    private Meeting completedMeeting;
    private Meeting plannedMeeting;
    private CaseRecord firstCaseRecord;
    private CaseRecord secondCaseRecord;

    @BeforeEach
    void setUp() {
        registry = new Registry("Kommunstyrelsen", "KS");
        setField(registry, "id", 1L);

        completedMeeting = new Meeting(
                registry,
                "KS april",
                LocalDateTime.of(2026, 4, 30, 13, 0),
                LocalDateTime.of(2026, 4, 30, 15, 0),
                "Sessionssalen",
                MeetingStatus.COMPLETED,
                "Anteckning"
        );
        setField(completedMeeting, "id", 10L);

        plannedMeeting = new Meeting(
                registry,
                "KS maj",
                LocalDateTime.of(2026, 5, 10, 13, 0),
                null,
                "Sessionssalen",
                MeetingStatus.PLANNED,
                null
        );
        setField(plannedMeeting, "id", 11L);

        firstCaseRecord = mock(CaseRecord.class);
        secondCaseRecord = mock(CaseRecord.class);
    }

    @Test
    @DisplayName("createProtocolForCompletedMeeting should create protocol with sequential paragraphs")
    void createProtocolForCompletedMeeting_shouldCreateProtocolWithSequentialParagraphs() {
        when(firstCaseRecord.getTitle()).thenReturn("Första ärendet");
        when(secondCaseRecord.getTitle()).thenReturn("Andra ärendet");

        completedMeeting.getAgendaItems().add(new MeetingAgendaItem(completedMeeting, firstCaseRecord, 1, null));
        completedMeeting.getAgendaItems().add(new MeetingAgendaItem(completedMeeting, secondCaseRecord, 2, null));

        when(meetingRepository.findById(10L)).thenReturn(Optional.of(completedMeeting));
        when(protocolRepository.existsByMeetingId(10L)).thenReturn(false);

        ProtocolParagraphSequence sequence = new ProtocolParagraphSequence(registry, 2026, 0L);
        when(sequenceRepository.findByRegistryIdAndYear(1L, 2026)).thenReturn(Optional.of(sequence));
        when(protocolRepository.save(any(Protocol.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Protocol result = protocolService.createProtocolForCompletedMeeting(10L);

        assertThat(result.getMeeting()).isEqualTo(completedMeeting);
        assertThat(result.getRegistry()).isEqualTo(registry);
        assertThat(result.getTitle()).isEqualTo("Protokoll - Kommunstyrelsen - 2026");
        assertThat(result.getYear()).isEqualTo(2026);

        assertThat(result.getParagraphs()).hasSize(2);
        assertThat(result.getParagraphs())
                .extracting(ProtocolParagraph::getParagraphNumber)
                .containsExactly(1L, 2L);

        assertThat(result.getParagraphs())
                .extracting(ProtocolParagraph::getHeading)
                .containsExactly(
                        "§ 1 Första ärendet",
                        "§ 2 Andra ärendet"
                );

        verify(protocolRepository).save(any(Protocol.class));
        verify(sequenceRepository, times(2)).saveAndFlush(sequence);
    }

    @Test
    @DisplayName("createProtocolForCompletedMeeting should continue paragraph sequence for same registry and year")
    void createProtocolForCompletedMeeting_shouldContinueParagraphSequenceForSameRegistryAndYear() {
        when(firstCaseRecord.getTitle()).thenReturn("Fjärde ärendet");
        completedMeeting.getAgendaItems().add(new MeetingAgendaItem(completedMeeting, firstCaseRecord, 1, null));

        when(meetingRepository.findById(10L)).thenReturn(Optional.of(completedMeeting));
        when(protocolRepository.existsByMeetingId(10L)).thenReturn(false);

        ProtocolParagraphSequence sequence = new ProtocolParagraphSequence(registry, 2026, 3L);
        when(sequenceRepository.findByRegistryIdAndYear(1L, 2026)).thenReturn(Optional.of(sequence));
        when(protocolRepository.save(any(Protocol.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Protocol result = protocolService.createProtocolForCompletedMeeting(10L);

        assertThat(result.getParagraphs()).hasSize(1);
        assertThat(result.getParagraphs().getFirst().getParagraphNumber()).isEqualTo(4L);
        assertThat(result.getParagraphs().getFirst().getHeading()).isEqualTo("§ 4 Fjärde ärendet");
    }

    @Test
    @DisplayName("createProtocolForCompletedMeeting should throw InvalidMeetingStateException when meeting is not completed")
    void createProtocolForCompletedMeeting_shouldThrowInvalidMeetingStateException_whenMeetingIsNotCompleted() {
        when(meetingRepository.findById(11L)).thenReturn(Optional.of(plannedMeeting));

        assertThatThrownBy(() -> protocolService.createProtocolForCompletedMeeting(11L))
                .isInstanceOf(InvalidMeetingStateException.class);
    }

    @Test
    @DisplayName("createProtocolForCompletedMeeting should throw ProtocolAlreadyExistsException when protocol already exists")
    void createProtocolForCompletedMeeting_shouldThrowProtocolAlreadyExistsException_whenProtocolAlreadyExists() {
        when(meetingRepository.findById(10L)).thenReturn(Optional.of(completedMeeting));
        when(protocolRepository.existsByMeetingId(10L)).thenReturn(true);

        assertThatThrownBy(() -> protocolService.createProtocolForCompletedMeeting(10L))
                .isInstanceOf(ProtocolAlreadyExistsException.class);
    }

    @Test
    @DisplayName("createProtocolForCompletedMeeting should throw MeetingNotFoundException when meeting does not exist")
    void createProtocolForCompletedMeeting_shouldThrowMeetingNotFoundException_whenMeetingDoesNotExist() {
        when(meetingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> protocolService.createProtocolForCompletedMeeting(999L))
                .isInstanceOf(MeetingNotFoundException.class);
    }

    @Test
    @DisplayName("updateParagraphDecision should update decision")
    void updateParagraphDecision_shouldUpdateDecision() {
        Protocol protocol = new Protocol(completedMeeting, registry, "Protokoll - Kommunstyrelsen - 2026", 2026);
        setField(protocol, "id", 1L);

        ProtocolParagraph paragraph = new ProtocolParagraph(firstCaseRecord, 1L, "§ 1 Första ärendet");
        protocol.addParagraph(paragraph);
        setField(paragraph, "id", 100L);

        when(paragraphRepository.findById(100L)).thenReturn(Optional.of(paragraph));
        when(protocolRepository.findWithLockById(1L)).thenReturn(Optional.of(protocol));

        Protocol result = protocolService.updateParagraphDecision(
                100L,
                ProtocolDecisionType.REJECTED,
                "Kommunstyrelsen beslutar att avslå ärendet."
        );

        assertThat(result).isEqualTo(protocol);
        assertThat(paragraph.getDecisionType()).isEqualTo(ProtocolDecisionType.REJECTED);
        assertThat(paragraph.getDecisionText()).isEqualTo("Kommunstyrelsen beslutar att avslå ärendet.");
    }

    @Test
    @DisplayName("updateParagraphDecision should throw ProtocolParagraphNotFoundException when paragraph does not exist")
    void updateParagraphDecision_shouldThrowProtocolParagraphNotFoundException_whenParagraphDoesNotExist() {
        when(paragraphRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> protocolService.updateParagraphDecision(
                999L,
                ProtocolDecisionType.APPROVED,
                "Kommunstyrelsen beslutar att bifalla ärendet."
        ))
                .isInstanceOf(ProtocolParagraphNotFoundException.class);
    }

    @Test
    @DisplayName("buildDefaultDecisionText should return approved text")
    void buildDefaultDecisionText_shouldReturnApprovedText() {
        Protocol protocol = new Protocol(completedMeeting, registry, "Protokoll - Kommunstyrelsen - 2026", 2026);
        ProtocolParagraph paragraph = new ProtocolParagraph(firstCaseRecord, 1L, "§ 1 Första ärendet");
        protocol.addParagraph(paragraph);

        when(paragraphRepository.findById(100L)).thenReturn(Optional.of(paragraph));

        String result = protocolService.buildDefaultDecisionText(100L, ProtocolDecisionType.APPROVED);

        assertThat(result).isEqualTo("Kommunstyrelsen beslutar att bifalla ärendet.");
    }

    @Test
    @DisplayName("buildDefaultDecisionText should return rejected text")
    void buildDefaultDecisionText_shouldReturnRejectedText() {
        Protocol protocol = new Protocol(completedMeeting, registry, "Protokoll - Kommunstyrelsen - 2026", 2026);
        ProtocolParagraph paragraph = new ProtocolParagraph(firstCaseRecord, 1L, "§ 1 Första ärendet");
        protocol.addParagraph(paragraph);

        when(paragraphRepository.findById(100L)).thenReturn(Optional.of(paragraph));

        String result = protocolService.buildDefaultDecisionText(100L, ProtocolDecisionType.REJECTED);

        assertThat(result).isEqualTo("Kommunstyrelsen beslutar att avslå ärendet.");
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

    @Test
    @DisplayName("updateParagraphDecision should throw ProtocolAlreadyArchivedException when protocol is archived")
    void updateParagraphDecision_shouldThrowProtocolAlreadyArchivedException_whenProtocolIsArchived() {
        Protocol protocol = new Protocol(completedMeeting, registry, "Protokoll - Kommunstyrelsen - 2026", 2026);
        setField(protocol, "id", 1L);

        ProtocolParagraph paragraph = new ProtocolParagraph(firstCaseRecord, 1L, "§ 1 Första ärendet");
        protocol.addParagraph(paragraph);
        setField(paragraph, "id", 100L);

        CaseFile archivedPdfFile = mock(CaseFile.class);
        protocol.setArchivedPdfFile(archivedPdfFile);

        when(paragraphRepository.findById(100L)).thenReturn(Optional.of(paragraph));
        when(protocolRepository.findWithLockById(1L)).thenReturn(Optional.of(protocol));

        assertThatThrownBy(() -> protocolService.updateParagraphDecision(
                100L,
                ProtocolDecisionType.APPROVED,
                "Kommunstyrelsen beslutar att bifalla ärendet."
        ))
                .isInstanceOf(ProtocolAlreadyArchivedException.class)
                .hasMessage("Protokollet är redan arkiverat och kan inte längre ändras.");

        assertThat(paragraph.getDecisionType()).isNull();
        assertThat(paragraph.getDecisionText()).isNull();
    }
}
