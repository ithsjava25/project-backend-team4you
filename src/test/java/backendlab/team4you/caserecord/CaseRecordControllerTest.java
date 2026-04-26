package backendlab.team4you.caserecord;

import backendlab.team4you.common.ConfidentialityLevel;
import backendlab.team4you.exceptions.GlobalRestExceptionHandler;
import backendlab.team4you.exceptions.RegistryNotFoundException;
import backendlab.team4you.exceptions.UserNotFoundException;
import backendlab.team4you.registryaccess.RegistryAccessService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CaseRecordController.class)
@Import(GlobalRestExceptionHandler.class)
class CaseRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CaseRecordService caseRecordService;

    @MockitoBean
    private RegistryAccessService registryAccessService;



    @Test
    @DisplayName("should create case record and return 201 created")
    @WithMockUser(username = "dev")
    void shouldCreateCaseRecordAndReturn201Created() throws Exception {
        String requestJson = """
                {
                  "registryId": 1,
                  "title": "Test case title",
                  "description": "Test description",
                  "status": "OPEN",
                  "ownerUserId": "owner-123",
                  "assignedUserId": "assigned-456",
                  "confidentialityLevel": "OPEN",
                  "openedAt": "2026-04-09T10:30:00"
                }
                """;

        CaseRecordResponseDto responseDto = new CaseRecordResponseDto(
                42L,
                "KS26-1",
                1L,
                "KS",
                "Test case title",
                "Test description",
                CaseStatus.OPEN,
                "owner-123",
                "assigned-456",
                ConfidentialityLevel.OPEN,
                java.time.LocalDateTime.of(2026, 4, 9, 10, 30),
                java.time.LocalDateTime.of(2026, 4, 9, 10, 31),
                null,
                null
        );

        when(caseRecordService.createCaseRecord(any(CaseRecordRequestDto.class)))
                .thenReturn(responseDto);

        when(registryAccessService.canCreateCasesInRegistry(any(), any()))
                .thenReturn(true);

        mockMvc.perform(post("/api/case-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/case-records/42"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.caseNumber").value("KS26-1"))
                .andExpect(jsonPath("$.registryId").value(1))
                .andExpect(jsonPath("$.registryCode").value("KS"))
                .andExpect(jsonPath("$.title").value("Test case title"))
                .andExpect(jsonPath("$.description").value("Test description"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.ownerUserId").value("owner-123"))
                .andExpect(jsonPath("$.assignedUserId").value("assigned-456"))
                .andExpect(jsonPath("$.confidentialityLevel").value("OPEN"));
    }

    @Test
    @DisplayName("should return 404 when registry does not exist")
    @WithMockUser(username = "dev")
    void shouldReturn404WhenRegistryDoesNotExist() throws Exception {
        String requestJson = """
                {
                  "registryId": 99,
                  "title": "Test case title",
                  "description": "Test description",
                  "status": "OPEN",
                  "ownerUserId": "owner-123",
                  "assignedUserId": "assigned-456",
                  "confidentialityLevel": "OPEN",
                  "openedAt": "2026-04-09T10:30:00"
                }
                """;

        when(caseRecordService.createCaseRecord(any(CaseRecordRequestDto.class)))
                .thenThrow(new RegistryNotFoundException("registry not found: 99"));

        when(registryAccessService.canCreateCasesInRegistry(any(), any()))
                .thenReturn(true);

        mockMvc.perform(post("/api/case-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("registry not found: 99"));
    }

    @Test
    @DisplayName("should return 404 when user does not exist")
    @WithMockUser(username = "dev")
    void shouldReturn404WhenUserDoesNotExist() throws Exception {
        String requestJson = """
                {
                  "registryId": 1,
                  "title": "Test case title",
                  "description": "Test description",
                  "status": "OPEN",
                  "ownerUserId": "missing-owner",
                  "assignedUserId": "assigned-456",
                  "confidentialityLevel": "OPEN",
                  "openedAt": "2026-04-09T10:30:00"
                }
                """;

        when(caseRecordService.createCaseRecord(any(CaseRecordRequestDto.class)))
                .thenThrow(new UserNotFoundException("user not found: missing-owner"));
        when(registryAccessService.canCreateCasesInRegistry(any(), any()))
                .thenReturn(true);

        mockMvc.perform(post("/api/case-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("user not found: missing-owner"));
    }

    @Test
    @DisplayName("should return 400 when request body is invalid")
    @WithMockUser(username = "dev")
    void shouldReturn400WhenRequestBodyIsInvalid() throws Exception {
        String invalidJson = """
                {
                  "registryId": null,
                  "title": "",
                  "description": "Test description",
                  "status": "OPEN",
                  "ownerUserId": "",
                  "assignedUserId": "",
                  "confidentialityLevel": "OPEN",
                  "openedAt": "2026-04-09T10:30:00"
                }
                """;
        when(registryAccessService.canCreateCasesInRegistry(any(), any()))
                .thenReturn(true);

        mockMvc.perform(post("/api/case-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
