package backendlab.team4you.dto;

import java.time.LocalDateTime;

public record UserResponseDTO(
        String id,
        String email,
        String displayName
) {}
