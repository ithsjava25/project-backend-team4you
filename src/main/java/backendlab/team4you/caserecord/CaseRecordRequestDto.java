package backendlab.team4you.caserecord;

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

    @Size(max = 50, message = "status must be at most 50 characters")
    String status,

    @NotBlank(message = "ownerUserId is required")
    String ownerUserId,

    @NotBlank(message = "assignedUserId is required")
    String assignedUserId,

    @Size(max = 50, message = "confidentiality level must be at most 50 characters")
    String confidentialityLevel,

    LocalDateTime openedAt
){
}
