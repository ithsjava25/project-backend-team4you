package backendlab.team4you.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    ApplicationRepository applicationRepository;

    @InjectMocks
    ApplicationService applicationService;

    private ApplicationEntity application;

    @BeforeEach
    void setUp() {
        application = new ApplicationEntity();
        application.setId(1L);
        application.setName("Test User");
        application.setEmail("test@test.com");
        application.setPhone("0701234567");
        application.setMessage("Test message");
    }

    @Test
    void save_shouldSaveApplication() {
        applicationService.save(application);

        verify(applicationRepository, times(1)).save(application);
    }

    @Test
    void delete_shouldDeleteApplication() {
        doNothing().when(applicationRepository).deleteById(1L);

        applicationService.delete(1L);

        verify(applicationRepository, times(1)).deleteById(1L);
    }

    @Test
    void findById_shouldReturnApplication_whenExists() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        ApplicationEntity result = applicationService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void findById_shouldReturnNull_whenNotFound() {
        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

        ApplicationEntity result = applicationService.findById(99L);

        assertNull(result);
    }

    @Test
    void getAll_shouldReturnAllApplications() {
        when(applicationRepository.findAll()).thenReturn(List.of(application));

        List<ApplicationEntity> result = applicationService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}