package backendlab.team4you.contact;

public record ContactFormDTO(
        String firstName,
        String lastName,
        String email,
        String phone,
        String message
) {}
