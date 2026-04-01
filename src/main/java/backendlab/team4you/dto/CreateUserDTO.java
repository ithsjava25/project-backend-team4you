package backendlab.team4you.dto;

import java.time.LocalDateTime;

public record CreateUserDTO(Long id, String firstName, String lastName, String passwordHash, LocalDateTime createdAt) {
}
