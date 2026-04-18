package backendlab.team4you.registry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistryRequestDto(

    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name must be at most 100 characters")
    String name,

    @NotBlank(message = "code is required")
    @Pattern(regexp = "[A-Z]{2}", message = "code must be exactly 2 uppercase letters")
    String code
){
}
