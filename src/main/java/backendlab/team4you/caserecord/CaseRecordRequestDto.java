package backendlab.team4you.caserecord;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class CaseRecordRequestDto {

    @NotNull(message = "registryId is required")
    Long registryId;

    @NotBlank(message = "title is required")
    @Size(max = 255, message = "title must be at most 255 characters")
    String title;

    String description;

    @NotNull(message = "ownerUserId is required")
    Long ownerUserId;

    @NotNull(message = "assignedUserId is required")
    Long assignedUserId;

    @Size(max = 50, message = "confidentiality level must be at most 50 characters")
    String confidentialityLevel;

    LocalDateTime openedAt;
}
