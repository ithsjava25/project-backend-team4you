package backendlab.team4you.registry;

import backendlab.team4you.exceptions.DuplicateRegistryCodeException;
import backendlab.team4you.exceptions.DuplicateRegistryNameException;
import backendlab.team4you.exceptions.GlobalRestExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegistryController.class)
@Import(GlobalRestExceptionHandler.class)
class RegistryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistryService registryService;

    @Test
    @DisplayName("should create registry and return 201 created")
    void shouldCreateRegistryAndReturn201Created() throws Exception {
        String requestJson = """
                {
                  "name": "Kommunstyrelsen",
                  "code": "KS"
                }
                """;

        RegistryResponseDto responseDto = new RegistryResponseDto(
                1L,
                "Kommunstyrelsen",
                "KS"
        );

        when(registryService.createRegistry(any(RegistryRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/registries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/registries/1"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Kommunstyrelsen"))
                .andExpect(jsonPath("$.code").value("KS"));
    }

    @Test
    @DisplayName("should return 409 when registry name already exists")
    void shouldReturn409WhenRegistryNameAlreadyExists() throws Exception {
        String requestJson = """
                {
                  "name": "Kommunstyrelsen",
                  "code": "KS"
                }
                """;

        when(registryService.createRegistry(any(RegistryRequestDto.class)))
                .thenThrow(new DuplicateRegistryNameException("registry name already exists: Kommunstyrelsen"));

        mockMvc.perform(post("/api/registries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("registry name already exists: Kommunstyrelsen"));
    }

    @Test
    @DisplayName("should return 409 when registry code already exists")
    void shouldReturn409WhenRegistryCodeAlreadyExists() throws Exception {
        String requestJson = """
                {
                  "name": "Kommunstyrelsen",
                  "code": "KS"
                }
                """;

        when(registryService.createRegistry(any(RegistryRequestDto.class)))
                .thenThrow(new DuplicateRegistryCodeException("registry code already exists: KS"));

        mockMvc.perform(post("/api/registries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("registry code already exists: KS"));
    }

    @Test
    @DisplayName("should return 400 when request body is invalid")
    void shouldReturn400WhenRequestBodyIsInvalid() throws Exception {
        String invalidJson = """
                {
                  "name": "",
                  "code": "k"
                }
                """;

        mockMvc.perform(post("/api/registries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
