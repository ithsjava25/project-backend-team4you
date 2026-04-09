package backendlab.team4you.caserecord;

import backendlab.team4you.exceptions.RegistryNotFoundException;
import backendlab.team4you.exceptions.UserNotFoundException;
import backendlab.team4you.registry.Registry;
import backendlab.team4you.registry.RegistryRepository;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.web.webauthn.api.Bytes;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseRecordServiceTest {

    @Mock
    private CaseRecordRepository caseRecordRepository;

    @Mock
    private CaseNumberSequenceRepository caseNumberSequenceRepository;

    @Mock
    private RegistryRepository registryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CaseRecordService caseRecordService;

    @Test
    @DisplayName("should create case record with next case number when all referenced entities exist")
    void shouldCreateCaseRecordWithNextCaseNumber() {
        Registry registry = new Registry("Kommunstyrelsen", "KS");

        UserEntity owner = new UserEntity(Bytes.random(), "owner@example.com", "Owner");
        UserEntity assignedUser = new UserEntity(Bytes.random(), "assigned@example.com", "Assigned");

        CaseRecordRequestDto requestDto = new CaseRecordRequestDto(
                1L,
                "test case",
                "test description",
                "OPEN",
                owner.getIdAsString(),
                assignedUser.getIdAsString(),
                "OPEN",
                LocalDateTime.of(2026, 4, 9, 10, 0)
        );

        CaseNumberSequence sequence = new CaseNumberSequence(registry, 2026, 0L);

        when(registryRepository.findById(1L)).thenReturn(Optional.of(registry));
        when(userRepository.findById(owner.getIdAsString())).thenReturn(Optional.of(owner));
        when(userRepository.findById(assignedUser.getIdAsString())).thenReturn(Optional.of(assignedUser));
        when(caseNumberSequenceRepository.findWithLockByRegistryAndYear(any(), any())).thenReturn(Optional.of(sequence));
        when(caseRecordRepository.save(any(CaseRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CaseRecordResponseDto response = caseRecordService.createCaseRecord(requestDto);

        assertThat(response.caseNumber()).isEqualTo("KS26-1");
        assertThat(response.title()).isEqualTo("test case");
        assertThat(response.registryCode()).isEqualTo("KS");
        assertThat(response.ownerUserId()).isEqualTo(owner.getIdAsString());
        assertThat(response.assignedUserId()).isEqualTo(assignedUser.getIdAsString());

        ArgumentCaptor<CaseRecord> caseRecordCaptor = ArgumentCaptor.forClass(CaseRecord.class);
        verify(caseRecordRepository).save(caseRecordCaptor.capture());

        CaseRecord savedCaseRecord = caseRecordCaptor.getValue();
        assertThat(savedCaseRecord.getCaseNumber()).isEqualTo("KS26-1");
        assertThat(savedCaseRecord.getTitle()).isEqualTo("test case");

        verify(caseNumberSequenceRepository).save(sequence);
        assertThat(sequence.getLastValue()).isEqualTo(1L);
    }

    @Test
    @DisplayName("should throw RegistryNotFoundException when registry does not exist")
    void shouldThrowWhenRegistryDoesNotExist() {
        CaseRecordRequestDto requestDto = new CaseRecordRequestDto(
                99L,
                "test case",
                "test description",
                "OPEN",
                "owner-id",
                "assigned-id",
                "OPEN",
                null
        );

        when(registryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseRecordService.createCaseRecord(requestDto))
                .isInstanceOf(RegistryNotFoundException.class)
                .hasMessage("registry not found: 99");
    }

    @Test
    @DisplayName("should throw UserNotFoundException when owner does not exist")
    void shouldThrowWhenOwnerDoesNotExist() {
        Registry registry = new Registry("Kommunstyrelsen", "KS");

        CaseRecordRequestDto requestDto = new CaseRecordRequestDto(
                1L,
                "test case",
                "test description",
                "OPEN",
                "missing-owner",
                "assigned-id",
                "OPEN",
                null
        );

        when(registryRepository.findById(1L)).thenReturn(Optional.of(registry));
        when(userRepository.findById("missing-owner")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseRecordService.createCaseRecord(requestDto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("user not found: missing-owner");
    }
}
