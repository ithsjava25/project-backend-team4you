package backendlab.team4you.protocol;

import backendlab.team4you.caserecord.CaseRecord;
import backendlab.team4you.exceptions.ProtocolNotFoundException;
import backendlab.team4you.exceptions.ProtocolNotReadyForPdfException;
import backendlab.team4you.meeting.Meeting;
import backendlab.team4you.meeting.MeetingStatus;
import backendlab.team4you.registry.Registry;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProtocolPdfServiceTest {

    @Mock
    private ProtocolRepository protocolRepository;
    private UserEntity viewer;

    @Mock
    private ProtocolViewService protocolViewService;

    private ProtocolPdfService protocolPdfService;

    private Registry registry;
    private Meeting meeting;
    private CaseRecord caseRecord;

    @BeforeEach
    void setUp() {
        protocolPdfService = new ProtocolPdfService(protocolRepository, protocolViewService);

        registry = new Registry("Kommunstyrelsen", "KS");
        setField(registry, "id", 1L);

        meeting = new Meeting(
                registry,
                "Kommunstyrelsen april",
                LocalDateTime.of(2026, 4, 27, 13, 0),
                LocalDateTime.of(2026, 4, 27, 15, 0),
                "Sessionssalen",
                MeetingStatus.COMPLETED,
                "Anteckning"
        );
        setField(meeting, "id", 10L);

        caseRecord = mock(CaseRecord.class);

        viewer = new UserEntity();
        viewer.setName("admin");
        viewer.setRole(UserRole.ADMIN);
    }

    @Test
    @DisplayName("generatePdf should throw ProtocolNotFoundException when protocol does not exist")
    void generatePdf_shouldThrowProtocolNotFoundException_whenProtocolDoesNotExist() {
        when(protocolRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> protocolPdfService.generatePdf(999L,viewer))
                .isInstanceOf(ProtocolNotFoundException.class);
    }

    @Test
    @DisplayName("generatePdf should throw ProtocolNotReadyForPdfException when protocol is not ready")
    void generatePdf_shouldThrowProtocolNotReadyForPdfException_whenProtocolIsNotReady() {
        Protocol protocol = new Protocol(
                meeting,
                registry,
                "Protokoll - Kommunstyrelsen - 2026",
                2026
        );
        setField(protocol, "id", 1L);

        ProtocolParagraph paragraph = new ProtocolParagraph(
                caseRecord,
                1L,
                "§ 1 Provärendet"
        );
        protocol.addParagraph(paragraph);

        when(protocolRepository.findById(1L)).thenReturn(Optional.of(protocol));

        assertThatThrownBy(() -> protocolPdfService.generatePdf(1L, viewer))
                .isInstanceOf(ProtocolNotReadyForPdfException.class)
                .hasMessage("Alla paragrafer måste ha beslut innan PDF kan skapas.");
    }

    @Test
    @DisplayName("generatePdf should return pdf bytes when protocol is ready")
    void generatePdf_shouldReturnPdfBytes_whenProtocolIsReady() {
        when(caseRecord.getCaseNumber()).thenReturn("KS26-1");

        Protocol protocol = new Protocol(
                meeting,
                registry,
                "Protokoll - Kommunstyrelsen - 2026",
                2026
        );
        setField(protocol, "id", 1L);

        ProtocolParagraph paragraph = new ProtocolParagraph(
                caseRecord,
                1L,
                "§ 1 Provärendet"
        );
        paragraph.updateDecision(
                ProtocolDecisionType.APPROVED,
                "Kommunstyrelsen beslutar att bifalla ärendet."
        );
        protocol.addParagraph(paragraph);

        when(protocolRepository.findById(1L)).thenReturn(Optional.of(protocol));
        when(protocolViewService.getParagraphsForViewer(1L, viewer))
                .thenReturn(List.of(new ProtocolParagraphViewDto(
                        1L,
                        "§ 1 Provärendet",
                        "KS26-1",
                        false,
                        ProtocolDecisionType.APPROVED,
                        "Bifall",
                        "Kommunstyrelsen beslutar att bifalla ärendet."
                )));

        byte[] result = protocolPdfService.generatePdf(1L, viewer);

        assertThat(result).isNotEmpty();
        assertThat(result[0]).isEqualTo((byte) '%');
        assertThat(result[1]).isEqualTo((byte) 'P');
        assertThat(result[2]).isEqualTo((byte) 'D');
        assertThat(result[3]).isEqualTo((byte) 'F');
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
