package backendlab.team4you.caserecord;

import backendlab.team4you.registry.Registry;
import backendlab.team4you.user.UserEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Transactional
public class CaseRecordService {
    private final CaseRecordRepository caseRecordRepository;
    private final CaseNumberSequenceRepository caseNumberSequenceRepository;

    public CaseRecordService(
            CaseRecordRepository caseRecordRepository,
            CaseNumberSequenceRepository caseNumberSequenceRepository
    ) {
        this.caseRecordRepository = caseRecordRepository;
        this.caseNumberSequenceRepository = caseNumberSequenceRepository;
    }

    public CaseRecord createCaseRecord(
            Registry registry,
            String title,
            String description,
            String status,
            UserEntity owner,
            UserEntity assignedUser,
            String confidentialityLevel,
            LocalDateTime openedAt
    ) {
        CaseRecord caseRecord = new CaseRecord(
                registry,
                title,
                description,
                status,
                owner,
                assignedUser,
                confidentialityLevel,
                openedAt
        );

        String nextCaseNumber = allocateNextCaseNumber(registry);
        caseRecord.setCaseNumber(nextCaseNumber);

        return caseRecordRepository.save(caseRecord);
    }

    private String allocateNextCaseNumber(Registry registry) {
        int year = LocalDateTime.now().getYear();

        CaseNumberSequence sequence = caseNumberSequenceRepository
                .findWithLockByRegistryAndYear(registry, year)
                .orElseGet(() -> new CaseNumberSequence(registry, year, 0L));

        long nextValue = sequence.getLastValue() + 1;
        sequence.setLastValue(nextValue);
        caseNumberSequenceRepository.save(sequence);

        return buildCaseNumber(registry, year, nextValue);
    }

    private String buildCaseNumber(Registry registry, int year, long sequence) {
        String shortYear = String.format("%02d", year % 100);
        return registry.getCode() + shortYear + "-" + sequence;
    }
}
