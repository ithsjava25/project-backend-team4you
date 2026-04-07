package backendlab.team4you.dto;

import java.time.LocalDateTime;

public record UserUpdateDTO(
        String firstName,
        String lastName,
        String phoneNumber
) {}
