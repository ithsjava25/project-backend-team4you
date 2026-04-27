package backendlab.team4you.caserecord;

import backendlab.team4you.common.ConfidentialityLevel;
import backendlab.team4you.exceptions.CaseRecordNotFoundException;
import backendlab.team4you.exceptions.RegistryNotFoundException;
import backendlab.team4you.exceptions.UserNotFoundException;
import backendlab.team4you.registry.Registry;
import backendlab.team4you.registry.RegistryRepository;
import backendlab.team4you.user.UserRepository;
import backendlab.team4you.user.UserEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CaseRecordService {
    private final CaseRecordRepository caseRecordRepository;
    private final CaseNumberSequenceRepository caseNumberSequenceRepository;
    private final RegistryRepository registryRepository;
    private final UserRepository userRepository;

    public CaseRecordService(
            CaseRecordRepository caseRecordRepository,
            CaseNumberSequenceRepository caseNumberSequenceRepository,
            RegistryRepository registryRepository,
            UserRepository userRepository
    ) {
        this.caseRecordRepository = caseRecordRepository;
        this.caseNumberSequenceRepository = caseNumberSequenceRepository;
        this.registryRepository = registryRepository;
        this.userRepository = userRepository;
    }

    public CaseRecordResponseDto createCaseRecord(CaseRecordRequestDto requestDto) {
        Registry registry = registryRepository.findById(requestDto.registryId())
                .orElseThrow(() -> new RegistryNotFoundException("registry not found: " + requestDto.registryId()));

        UserEntity owner = userRepository.findById(requestDto.ownerUserId())
                .orElseThrow(() -> new UserNotFoundException("user not found: " + requestDto.ownerUserId()));

        UserEntity assignedUser = null;
        if (requestDto.assignedUserId() != null && !requestDto.assignedUserId().isBlank()) {
            assignedUser = userRepository.findById(requestDto.assignedUserId())
                    .orElseThrow(() -> new UserNotFoundException("user not found: " + requestDto.assignedUserId()));
        }

        CaseRecord caseRecord = new CaseRecord(
                registry,
                requestDto.title(),
                requestDto.description(),
                requestDto.status(),
                owner,
                assignedUser,
                requestDto.confidentialityLevel(),
                requestDto.openedAt()
        );

        String nextCaseNumber = allocateNextCaseNumber(registry);
        caseRecord.setCaseNumber(nextCaseNumber);

        CaseRecord savedCaseRecord = caseRecordRepository.save(caseRecord);

        return toResponseDto(savedCaseRecord);
    }

    private String allocateNextCaseNumber(Registry registry) {
        return allocateNextCaseNumber(registry, LocalDateTime.now().getYear());
    }

    private String tryAllocateNextCaseNumber(Registry registry, int year) {
        CaseNumberSequence sequence = caseNumberSequenceRepository
                .findWithLockByRegistryAndYear(registry, year)
                .orElseGet(() -> caseNumberSequenceRepository.saveAndFlush(
                        new CaseNumberSequence(registry, year, 0L)
                ));

        long nextValue = sequence.getLastValue() + 1;
        sequence.setLastValue(nextValue);
        caseNumberSequenceRepository.save(sequence);

        return buildCaseNumber(registry, year, nextValue);
    }

    private String buildCaseNumber(Registry registry, int year, long sequence) {
        String shortYear = String.format("%02d", year % 100);
        return registry.getCode() + shortYear + "-" + sequence;
    }
    private CaseRecordResponseDto toResponseDto(CaseRecord caseRecord) {
        return new CaseRecordResponseDto(
                caseRecord.getId(),
                caseRecord.getCaseNumber(),
                caseRecord.getRegistry().getId(),
                caseRecord.getRegistry().getCode(),
                caseRecord.getTitle(),
                caseRecord.getDescription(),
                caseRecord.getStatus(),
                caseRecord.getOwner().getIdAsString(),
                caseRecord.getAssignedUser() != null ? caseRecord.getAssignedUser().getIdAsString() : null,
                caseRecord.getConfidentialityLevel(),
                caseRecord.getOpenedAt(),
                caseRecord.getCreatedAt(),
                caseRecord.getUpdatedAt(),
                caseRecord.getClosedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<CaseRecordResponseDto> findByRegistryId(Long registryId) {
        Registry registry = registryRepository.findById(registryId)
                .orElseThrow(() -> new RegistryNotFoundException("registry not found: " + registryId));

        return caseRecordRepository.findByRegistryIdOrderByCreatedAtDesc(registry.getId()).stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CaseRecordResponseDto findById(Long caseRecordId) {
        CaseRecord caseRecord = caseRecordRepository.findById(caseRecordId)
                .orElseThrow(() -> new CaseRecordNotFoundException(caseRecordId));

        return toResponseDto(caseRecord);
    }

    public CaseRecordResponseDto updateCaseRecord(Long caseRecordId, CaseStatus status, String assignedUserId) {
        CaseRecord caseRecord = caseRecordRepository.findById(caseRecordId)
                .orElseThrow(() -> new CaseRecordNotFoundException(caseRecordId));

        caseRecord.setStatus(status);

        if (assignedUserId == null || assignedUserId.isBlank()) {
            caseRecord.setAssignedUser(null);
        } else {
            UserEntity assignedUser = userRepository.findById(assignedUserId)
                    .orElseThrow(() -> new UserNotFoundException("user not found: " + assignedUserId));
            caseRecord.setAssignedUser(assignedUser);
        }

        CaseRecord savedCaseRecord = caseRecordRepository.save(caseRecord);
        return toResponseDto(savedCaseRecord);
    }

    @Transactional
    public CaseRecord findOrCreateAnnualProtocolCase(
            Registry registry,
            int year,
            UserEntity currentUser
    ) {
        String title = "Protokoll för " + registry.getName() + " år " + year;

        return caseRecordRepository
                .findByRegistryAndTitle(registry, title)
                .orElseGet(() -> createAnnualProtocolCase(registry, year, title, currentUser));
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("status is required");
        }

        String normalizedStatus = status.trim().toUpperCase();

        if (!normalizedStatus.equals("OPEN")
                && !normalizedStatus.equals("CLOSED")) {
            throw new IllegalArgumentException("invalid status: " + status);
        }

        return normalizedStatus;
    }

    private CaseRecord createAnnualProtocolCase(
            Registry registry,
            int year,
            String title,
            UserEntity currentUser
    ) {
        CaseRecord caseRecord = new CaseRecord(
                registry,
                title,
                "Årsärende för protokoll inom " + registry.getName() + " år " + year,
                CaseStatus.OPEN,
                currentUser,
                null,
                ConfidentialityLevel.OPEN,
                LocalDateTime.of(year, 1, 1, 0, 0)
        );

        String caseNumber = allocateNextCaseNumber(registry, year);
        caseRecord.setCaseNumber(caseNumber);

        return caseRecordRepository.save(caseRecord);
    }

    private String allocateNextCaseNumber(Registry registry, int year) {
        int maxAttempts = 3;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return tryAllocateNextCaseNumber(registry, year);
            } catch (DataIntegrityViolationException exception) {
                if (attempt == maxAttempts) {
                    throw new IllegalStateException(
                            "failed to allocate case number after " + maxAttempts +
                                    " attempts for registry " + registry.getId() +
                                    " and year " + year,
                            exception
                    );
                }
            }
        }

        throw new IllegalStateException(
                "unreachable state while allocating case number for registry " +
                        registry.getId() + " and year " + year
        );
    }
}
