package backendlab.team4you.dto;

import java.time.LocalDateTime;

public record UserDTO(Long id, String firstName, String lastName, String passwordHash, LocalDateTime createdAt) {
}
