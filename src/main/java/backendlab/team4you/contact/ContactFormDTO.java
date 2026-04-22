package backendlab.team4you.contact;

import jakarta.validation.constraints.*;

public record ContactFormDTO(
        @NotBlank(message = "First name cannot be blank")
        @Size(min = 2, max = 50)
        String firstName,

        @NotBlank(message = "Last name cannot be blank")
        @Size(min = 2, max = 50)
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^(?=.{7,20}$)(?=(?:\\D*\\d){7,15}\\D*$)\\+?[0-9][0-9\\s-]*[0-9]$", message = "Phone number is invalid")
        String phone,

        @NotBlank(message = "Message cannot be empty")
        @Size(min = 10, max = 1000)
        String message
) {}
