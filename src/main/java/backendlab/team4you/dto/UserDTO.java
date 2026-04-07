package backendlab.team4you.dto;

public record UserDTO(
        org.springframework.security.web.webauthn.api.Bytes id,
        String firstName,
        String lastName,
        String email,
        String fullName
) {}