package backendlab.team4you.protocol;

import backendlab.team4you.caserecord.CaseRecord;
import backendlab.team4you.caserecord.CaseStatus;
import backendlab.team4you.common.ConfidentialityLevel;
import backendlab.team4you.exceptions.ProtocolNotFoundException;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProtocolViewServiceTest {

    @Mock
    private ProtocolRepository protocolRepository;

    private ProtocolViewService protocolViewService;

    private Registry registry;
    private Meeting meeting;
    private UserEntity admin;
    private UserEntity assignedCaseOfficer;
    private UserEntity otherCaseOfficer;
    private UserEntity normalUser;

    @BeforeEach
    void setUp() {
        protocolViewService = new ProtocolViewService(protocolRepository);

        registry = new Registry("Kommunstyrelsen", "KS");
        setField(registry, "id", 1L);

        meeting = new Meeting(
                registry,
                "KS april",
                LocalDateTime.of(2026, 4, 27, 13, 0),
                LocalDateTime.of(2026, 4, 27, 15, 0),
                "Sessionssalen",
                MeetingStatus.COMPLETED,
                null
        );

        admin = user("admin-id", "admin", UserRole.ADMIN);
        assignedCaseOfficer = user("officer-1", "caseofficer1", UserRole.CASE_OFFICER);
        otherCaseOfficer = user("officer-2", "caseofficer2", UserRole.CASE_OFFICER);
        normalUser = user("user-id", "user", UserRole.USER);
    }

    @Test
    @DisplayName("should show confidential decision to admin")
    void shouldShowConfidentialDecisionToAdmin() {
        Protocol protocol = createProtocolWithCase(
                ConfidentialityLevel.CONFIDENTIAL,
                assignedCaseOfficer
        );

        when(protocolRepository.findById(1L)).thenReturn(Optional.of(protocol));

        List<ProtocolParagraphViewDto> result =
                protocolViewService.getParagraphsForViewer(1L, admin);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().decisionRestricted()).isFalse();
        assertThat(result.getFirst().decisionLabel()).isEqualTo("Bifall");
        assertThat(result.getFirst().decisionText())
                .isEqualTo("Kommunstyrelsen beslutar att bifalla ärendet.");
    }

    @Test
    @DisplayName("should show confidential decision to assigned case officer")
    void shouldShowConfidentialDecisionToAssignedCaseOfficer() {
        Protocol protocol = createProtocolWithCase(
                ConfidentialityLevel.CONFIDENTIAL,
                assignedCaseOfficer
        );

        when(protocolRepository.findById(1L)).thenReturn(Optional.of(protocol));

        List<ProtocolParagraphViewDto> result =
                protocolViewService.getParagraphsForViewer(1L, assignedCaseOfficer);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().decisionRestricted()).isFalse();
        assertThat(result.getFirst().decisionLabel()).isEqualTo("Bifall");
        assertThat(result.getFirst().decisionText())
                .isEqualTo("Kommunstyrelsen beslutar att bifalla ärendet.");
    }

    @Test
    @DisplayName("should hide confidential decision from other case officer")
    void shouldHideConfidentialDecisionFromOtherCaseOfficer() {
        Protocol protocol = createProtocolWithCase(
                ConfidentialityLevel.CONFIDENTIAL,
                assignedCaseOfficer
        );

        when(protocolRepository.findById(1L)).thenReturn(Optional.of(protocol));

        List<ProtocolParagraphViewDto> result =
                protocolViewService.getParagraphsForViewer(1L, otherCaseOfficer);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().decisionRestricted()).isTrue();
        assertThat(result.getFirst().decisionLabel()).isNull();
        assertThat(result.getFirst().decisionText())
                .isEqualTo("Beslutet omfattas av sekretess.");
    }

    @Test
    @DisplayName("should hide confidential decision from normal user")
    void shouldHideConfidentialDecisionFromNormalUser() {
        Protocol protocol = createProtocolWithCase(
                ConfidentialityLevel.CONFIDENTIAL,
                assignedCaseOfficer
        );

        when(protocolRepository.findById(1L)).thenReturn(Optional.of(protocol));

        List<ProtocolParagraphViewDto> result =
                protocolViewService.getParagraphsForViewer(1L, normalUser);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().decisionRestricted()).isTrue();
        assertThat(result.getFirst().decisionLabel()).isNull();
        assertThat(result.getFirst().decisionText())
                .isEqualTo("Beslutet omfattas av sekretess.");
    }

    @Test
    @DisplayName("should show open decision to normal user")
    void shouldShowOpenDecisionToNormalUser() {
        Protocol protocol = createProtocolWithCase(
                ConfidentialityLevel.OPEN,
                assignedCaseOfficer
        );

        when(protocolRepository.findById(1L)).thenReturn(Optional.of(protocol));

        List<ProtocolParagraphViewDto> result =
                protocolViewService.getParagraphsForViewer(1L, normalUser);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().decisionRestricted()).isFalse();
        assertThat(result.getFirst().decisionLabel()).isEqualTo("Bifall");
        assertThat(result.getFirst().decisionText())
                .isEqualTo("Kommunstyrelsen beslutar att bifalla ärendet.");
    }

    @Test
    @DisplayName("should throw ProtocolNotFoundException when protocol does not exist")
    void shouldThrowProtocolNotFoundException_whenProtocolDoesNotExist() {
        when(protocolRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> protocolViewService.getParagraphsForViewer(999L, normalUser))
                .isInstanceOf(ProtocolNotFoundException.class);
    }

    private Protocol createProtocolWithCase(
            ConfidentialityLevel confidentialityLevel,
            UserEntity assignedUser
    ) {
        CaseRecord caseRecord = new CaseRecord(
                registry,
                "Provärendet",
                "Beskrivning",
                CaseStatus.OPEN,
                admin,
                assignedUser,
                confidentialityLevel,
                LocalDateTime.of(2026, 4, 1, 9, 0)
        );
        setField(caseRecord, "id", 100L);
        caseRecord.setCaseNumber("KS26-1");

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

        return protocol;
    }

    private UserEntity user(String id, String name, UserRole role) {
        UserEntity user = new UserEntity();
        setField(user, "id", id);
        user.setName(name);
        user.setRole(role);
        return user;
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
