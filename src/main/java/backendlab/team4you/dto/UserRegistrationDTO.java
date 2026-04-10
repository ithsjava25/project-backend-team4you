package backendlab.team4you.dto;

import java.time.LocalDateTime;

public record UserRegistrationDTO(
        String name,
        String firstName,
        String lastName,
         String email,
       String phoneNumber,
         String password,
         String confirmPassword) {
}
