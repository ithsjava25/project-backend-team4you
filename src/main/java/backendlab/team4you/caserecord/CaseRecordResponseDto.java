package backendlab.team4you.caserecord;

import java.time.LocalDateTime;

public record CaseRecordResponseDto(
        Long id,
        String caseNumber,
        Long registryId,
        String registryCode,
        String title,
        String description,
        String status,
        Long ownerUserId,
        Long assignedUserId,
        String confidentialityLevel,
        LocalDateTime openedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime closedAt
){


}
