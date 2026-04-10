package backendlab.team4you.caserecord;

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

        UserEntity assignedUser = userRepository.findById(requestDto.assignedUserId())
                .orElseThrow(() -> new UserNotFoundException("user not found: " + requestDto.assignedUserId()));

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
        int year = LocalDateTime.now().getYear();
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
                caseRecord.getAssignedUser().getIdAsString(),
                caseRecord.getConfidentialityLevel(),
                caseRecord.getOpenedAt(),
                caseRecord.getCreatedAt(),
                caseRecord.getUpdatedAt(),
                caseRecord.getClosedAt()
        );
    }
}
