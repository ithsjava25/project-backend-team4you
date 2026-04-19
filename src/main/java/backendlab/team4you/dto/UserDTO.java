package backendlab.team4you.dto;

public record UserDTO(
        Long id,
        String firstName,
        String lastName,
        String email,
        String fullName
) {}