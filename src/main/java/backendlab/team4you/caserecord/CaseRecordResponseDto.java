package backendlab.team4you.caserecord;

import backendlab.team4you.common.ConfidentialityLevel;

import java.time.LocalDateTime;

public record CaseRecordResponseDto(
        Long id,
        String caseNumber,
        Long registryId,
        String registryCode,
        String title,
        String description,
        CaseStatus status,
        String ownerUserId,
        String assignedUserId,
        ConfidentialityLevel confidentialityLevel,
        LocalDateTime openedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime closedAt
){
}
