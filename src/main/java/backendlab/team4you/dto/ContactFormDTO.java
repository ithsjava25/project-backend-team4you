package backendlab.team4you.dto;

public record ContactFormDTO(
        String firstName,
        String lastName,
        String email,
        String phone,
        String message
) {}
