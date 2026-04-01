package backendlab.team4you.dto;

import java.time.LocalDateTime;

public record UpdateUserDTO(Long id, String firstName, String lastName, String passwordHash, LocalDateTime createdAt) {
}
