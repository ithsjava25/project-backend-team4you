package backendlab.team4you.registry;

import backendlab.team4you.exceptions.DuplicateRegistryCodeException;
import backendlab.team4you.exceptions.DuplicateRegistryNameException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegistryServiceTest {

    @Mock
    private RegistryRepository registryRepository;

    @Mock
    private RegistryService registryService;

    @Test
    @DisplayName("should create registry when name and code are unique")
    void shouldCreateRegistryWhenNameAndCodeAreUnique() {
        RegistryRequestDto requestDto = new RegistryRequestDto("Kommunstyrelsen", "KS");

        Registry savedRegistry = new Registry("Kommunstyrelsen", "KS");

        when(registryRepository.existsByName("Kommunstyrelsen")).thenReturn(false);
        when(registryRepository.existsByCode("KS")).thenReturn(false);
        when(registryRepository.save(any(Registry.class))).thenReturn(savedRegistry);

        RegistryResponseDto response = registryService.createRegistry(requestDto);

        assertThat(response.name()).isEqualTo("Kommunstyrelsen");
        assertThat(response.code()).isEqualTo("KS");

        ArgumentCaptor<Registry> registryCaptor = ArgumentCaptor.forClass(Registry.class);
        verify(registryRepository).save(registryCaptor.capture());

        Registry registryToSave = registryCaptor.getValue();
        assertThat(registryToSave.getName()).isEqualTo("Kommunstyrelsen");
        assertThat(registryToSave.getCode()).isEqualTo("KS");
    }

    @Test
    @DisplayName("should throw when registry name already exists")
    void shouldThrowWhenRegistryNameAlreadyExists() {
        RegistryRequestDto requestDto = new RegistryRequestDto("Kommunstyrelsen", "KS");

        when(registryRepository.existsByName("Kommunstyrelsen")).thenReturn(true);

        assertThatThrownBy(() -> registryService.createRegistry(requestDto))
                .isInstanceOf(DuplicateRegistryNameException.class)
                .hasMessage("registry name already exists: Kommunstyrelsen");

        verify(registryRepository, never()).save(any(Registry.class));
    }

    @Test
    @DisplayName("should throw when registry code already exists")
    void shouldThrowWhenRegistryCodeAlreadyExists() {
        RegistryRequestDto requestDto = new RegistryRequestDto("Kommunstyrelsen", "KS");

        when(registryRepository.existsByName("Kommunstyrelsen")).thenReturn(false);
        when(registryRepository.existsByCode("KS")).thenReturn(true);

        assertThatThrownBy(() -> registryService.createRegistry(requestDto))
                .isInstanceOf(DuplicateRegistryCodeException.class)
                .hasMessage("registry code already exists: KS");

        verify(registryRepository, never()).save(any(Registry.class));
    }

}
