package backendlab.team4you.casefile.ui;

import backendlab.team4you.casefile.CaseFileService;
import backendlab.team4you.common.ConfidentialityLevel;
import backendlab.team4you.exceptions.InvalidFileNameException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CaseFileViewController.class)
class CaseFileViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CaseFileService caseFileService;

    @Test
    @DisplayName("uploadCaseFile should return fragment with success message when upload succeeds")
    void uploadCaseFile_shouldReturnFragmentWithSuccessMessage_whenUploadSucceeds() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "hello".getBytes()
        );

        when(caseFileService.listFiles(1L)).thenReturn(List.of());

        mockMvc.perform(multipart("/dashboard/case-management/case-records/{caseId}/files", 1L)
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/case-management/case-file-list :: caseFileList"))
                .andExpect(model().attribute("caseRecordId", 1L))
                .andExpect(model().attributeExists("files"))
                .andExpect(model().attribute("successMessage", "Filen laddades upp."));
    }

    @Test
    @DisplayName("uploadCaseFile should return fragment with error message when upload fails")
    void uploadCaseFile_shouldReturnFragmentWithErrorMessage_whenUploadFails() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "bad.pdf",
                "application/pdf",
                "hello".getBytes()
        );

        when(caseFileService.uploadFile(eq(1L), any(), ConfidentialityLevel.OPEN))
                .thenThrow(new InvalidFileNameException("Filnamn måste anges."));
        when(caseFileService.listFiles(1L)).thenReturn(List.of());

        mockMvc.perform(multipart("/dashboard/case-management/case-records/{caseId}/files", 1L)
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/case-management/case-file-list :: caseFileList"))
                .andExpect(model().attribute("errorMessage", "Filnamn måste anges."));
    }

    @Test
    @DisplayName("deleteCaseFile should return fragment with success message when delete succeeds")
    void deleteCaseFile_shouldReturnFragmentWithSuccessMessage_whenDeleteSucceeds() throws Exception {
        when(caseFileService.listFiles(1L)).thenReturn(List.of());

        mockMvc.perform(delete("/dashboard/case-management/case-records/{caseId}/files/{fileId}", 1L, 100L))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/case-management/case-file-list :: caseFileList"))
                .andExpect(model().attribute("successMessage", "Filen togs bort."))
                .andExpect(model().attribute("caseRecordId", 1L));
    }

    @Test
    @DisplayName("deleteCaseFile should return fragment with error message when delete fails")
    void deleteCaseFile_shouldReturnFragmentWithErrorMessage_whenDeleteFails() throws Exception {
        doThrow(new InvalidFileNameException("Kunde inte ta bort filen."))
                .when(caseFileService).deleteFile(1L, 100L);
        when(caseFileService.listFiles(1L)).thenReturn(List.of());

        mockMvc.perform(delete("/dashboard/case-management/case-records/{caseId}/files/{fileId}", 1L, 100L))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/case-management/case-file-list :: caseFileList"))
                .andExpect(model().attribute("errorMessage", "Kunde inte ta bort filen."));
    }
}
