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
import org.springframework.dao.DataIntegrityViolationException;
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

        UserEntity owner = new UserEntity();
        owner.setId(1L);

        UserEntity assignedUser = new UserEntity();
        assignedUser.setId(2L);

        CaseRecordRequestDto requestDto = new CaseRecordRequestDto(
                1L,
                "test case",
                "test description",
                "OPEN",
                owner.getId(),
                assignedUser.getId(),
                "OPEN",
                LocalDateTime.of(2026, 4, 9, 10, 0)
        );


        CaseNumberSequence sequence = new CaseNumberSequence(registry, 2026, 0L);

        when(registryRepository.findById(1L)).thenReturn(Optional.of(registry));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(userRepository.findById(assignedUser.getId())).thenReturn(Optional.of(assignedUser));
        when(caseNumberSequenceRepository.findWithLockByRegistryAndYear(any(), any())).thenReturn(Optional.of(sequence));
        when(caseRecordRepository.save(any(CaseRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CaseRecordResponseDto response = caseRecordService.createCaseRecord(requestDto);

        assertThat(response.caseNumber()).isEqualTo("KS26-1");
        assertThat(response.title()).isEqualTo("test case");
        assertThat(response.registryCode()).isEqualTo("KS");
        assertThat(response.ownerUserId()).isEqualTo(owner.getId());
        assertThat(response.assignedUserId()).isEqualTo(assignedUser.getId());

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
                123L,
                456L,
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
                123L,
                456L,
                "OPEN",
                null
        );

        when(registryRepository.findById(1L)).thenReturn(Optional.of(registry));
        when(userRepository.findById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseRecordService.createCaseRecord(requestDto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("user not found: 123");
    }

    @Test
    @DisplayName("should retry sequence creation when first insert collides and then succeed")
    void shouldRetrySequenceCreationWhenFirstInsertCollidesAndThenSucceed() {
        Registry registry = new Registry("Kommunstyrelsen", "KS");

        UserEntity owner = new UserEntity();
        owner.setId(1L);
        owner.setUsername("owner@example.com");
        owner.setDisplayName("Owner");

        UserEntity assignedUser = new UserEntity();
        assignedUser.setId(2L);
        assignedUser.setUsername("assigned@example.com");
        assignedUser.setDisplayName("Assigned");

        CaseRecordRequestDto requestDto = new CaseRecordRequestDto(
                1L,
                "test case",
                "test description",
                "OPEN",
                owner.getId(),
                assignedUser.getId(),
                "OPEN",
                LocalDateTime.of(2026, 4, 9, 10, 0)
        );

        CaseNumberSequence existingSequenceAfterRetry = new CaseNumberSequence(registry, 2026, 0L);

        when(registryRepository.findById(1L)).thenReturn(Optional.of(registry));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(userRepository.findById(assignedUser.getId())).thenReturn(Optional.of(assignedUser));

        when(caseNumberSequenceRepository.findWithLockByRegistryAndYear(any(), any()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existingSequenceAfterRetry));

        when(caseNumberSequenceRepository.saveAndFlush(any(CaseNumberSequence.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        when(caseNumberSequenceRepository.save(any(CaseNumberSequence.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(caseRecordRepository.save(any(CaseRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CaseRecordResponseDto response = caseRecordService.createCaseRecord(requestDto);

        assertThat(response.caseNumber()).isEqualTo("KS26-1");
        assertThat(response.registryCode()).isEqualTo("KS");

        verify(caseNumberSequenceRepository, times(2))
                .findWithLockByRegistryAndYear(any(), any());

        verify(caseNumberSequenceRepository, times(1))
                .saveAndFlush(any(CaseNumberSequence.class));

        assertThat(existingSequenceAfterRetry.getLastValue()).isEqualTo(1L);

        ArgumentCaptor<CaseRecord> caseRecordCaptor = ArgumentCaptor.forClass(CaseRecord.class);
        verify(caseRecordRepository).save(caseRecordCaptor.capture());

        assertThat(caseRecordCaptor.getValue().getCaseNumber()).isEqualTo("KS26-1");
    }

    @Test
    @DisplayName("should throw IllegalStateException after max retry attempts when sequence creation keeps failing")
    void shouldThrowIllegalStateExceptionAfterMaxRetryAttempts() {
        Registry registry = new Registry("Kommunstyrelsen", "KS");

        UserEntity owner = new UserEntity();
        owner.setId(1L);
        owner.setUsername("owner@example.com");
        owner.setDisplayName("Owner");

        UserEntity assignedUser = new UserEntity();
        assignedUser.setId(2L);
        assignedUser.setUsername("assigned@example.com");
        assignedUser.setDisplayName("Assigned");

        CaseRecordRequestDto requestDto = new CaseRecordRequestDto(
                1L,
                "test case",
                "test description",
                "OPEN",
                owner.getId(),
                assignedUser.getId(),
                "OPEN",
                LocalDateTime.of(2026, 4, 9, 10, 0)
        );

        when(registryRepository.findById(1L)).thenReturn(Optional.of(registry));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(userRepository.findById(assignedUser.getId())).thenReturn(Optional.of(assignedUser));

        when(caseNumberSequenceRepository.findWithLockByRegistryAndYear(any(), any()))
                .thenReturn(Optional.empty());

        when(caseNumberSequenceRepository.saveAndFlush(any(CaseNumberSequence.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        caseRecordService.createCaseRecord(requestDto)
                )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("failed to allocate case number after 3 attempts");

        verify(caseNumberSequenceRepository, times(3))
                .findWithLockByRegistryAndYear(any(), any());

        verify(caseNumberSequenceRepository, times(3))
                .saveAndFlush(any(CaseNumberSequence.class));

        verify(caseRecordRepository, never()).save(any(CaseRecord.class));
    }
}
