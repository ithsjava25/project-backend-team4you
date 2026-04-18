package backendlab.team4you.registry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RegistryTest {

    @Test
    @DisplayName("should create registry when name and code are valid")
    void shouldCreateRegistryWhenNameAndCodeAreValid() {
        Registry registry = new Registry("Kommunstyrelsen", "KS");

        assertThat(registry.getName()).isEqualTo("Kommunstyrelsen");
        assertThat(registry.getCode()).isEqualTo("KS");
    }

    @Test
    @DisplayName("should trim name when creating registry")
    void shouldTrimNameWhenCreatingRegistry() {
        Registry registry = new Registry("  Kommunstyrelsen  ", "KS");
        assertThat(registry.getName()).isEqualTo("Kommunstyrelsen");
        assertThat(registry.getCode()).isEqualTo("KS");
    }

    @Test
    @DisplayName("should throw when name is blank")
    void shouldThrowWhenNameIsBlank() {
        assertThatThrownBy(() -> new Registry("   ", "KS"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name is required");
    }

    @Test
    @DisplayName("should throw when code is null")
    void shouldThrowWhenCodeIsNull() {
        assertThatThrownBy(() -> new Registry("Kommunstyrelsen", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("code must be exactly 2 uppercase letters");
    }

    @Test
    @DisplayName("should throw when code is not two uppercase letters")
    void shouldThrowWhenCodeIsInvalid() {
        assertThatThrownBy(() -> new Registry("Kommunstyrelsen", "ks"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("code must be exactly 2 uppercase letters");
    }

}
