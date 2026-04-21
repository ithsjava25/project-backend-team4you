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
    void findById_shouldReturnApplication_whenExists() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        ApplicationEntity result = applicationService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }
}