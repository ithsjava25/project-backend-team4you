package backendlab.team4you.caserecord;

import backendlab.team4you.common.ConfidentialityLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CaseRecordRequestDto(

        @NotNull(message = "registryId is required")
        Long registryId,

        @NotBlank(message = "title is required")
        @Size(max = 255, message = "title must be at most 255 characters")
        String title,

        String description,

        @NotNull(message = "status is required")
        CaseStatus status,

        @NotBlank(message = "ownerUserId is required")
        String ownerUserId,

        String assignedUserId,

        @NotNull(message = "confidentiality level is required")
        ConfidentialityLevel confidentialityLevel,

        LocalDateTime openedAt
) {
}