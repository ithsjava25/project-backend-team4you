package backendlab.team4you.casefile;

import backendlab.team4you.exceptions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CaseFileController.class)
@Import(ApiExceptionHandler.class)
class CaseFileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CaseFileService caseFileService;

    @Test
    @DisplayName("uploadFile should return ok and response body when upload succeeds")
    void uploadFile_shouldReturnOkAndResponseBody_whenUploadSucceeds() throws Exception {
        CaseFile savedFile = new CaseFile();
        savedFile.setId(10L);
        savedFile.setOriginalFilename("test.pdf");
        savedFile.setContentType("application/pdf");
        savedFile.setSize(123L);
        savedFile.setUploadedAt(LocalDateTime.of(2026, 4, 16, 12, 0));
        savedFile.setDocumentNumber(1);
        savedFile.setDocumentReference("KS26-1-1");

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "hello".getBytes()
        );

        when(caseFileService.uploadFile(eq(1L), any())).thenReturn(savedFile);

        mockMvc.perform(multipart("/api/cases/{caseRecordId}/files", 1L)
                        .file(multipartFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.originalFilename").value("test.pdf"))
                .andExpect(jsonPath("$.contentType").value("application/pdf"))
                .andExpect(jsonPath("$.size").value(123))
                .andExpect(jsonPath("$.documentNumber").value(1))
                .andExpect(jsonPath("$.documentReference").value("KS26-1-1"));
    }

    @Test
    @DisplayName("uploadFile should return not found when case record does not exist")
    void uploadFile_shouldReturnNotFound_whenCaseRecordDoesNotExist() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "hello".getBytes()
        );

        when(caseFileService.uploadFile(eq(99L), any()))
                .thenThrow(new CaseRecordNotFoundException(99L));

        mockMvc.perform(multipart("/api/cases/{caseRecordId}/files", 99L)
                        .file(multipartFile))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("not found"))
                .andExpect(jsonPath("$.message").value("Case record not found: 99"));
    }

    @Test
    @DisplayName("uploadFile should return bad request when filename is invalid")
    void uploadFile_shouldReturnBadRequest_whenFilenameIsInvalid() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "",
                "application/pdf",
                "hello".getBytes()
        );

        when(caseFileService.uploadFile(eq(1L), any()))
                .thenThrow(new InvalidFileNameException("Filename must not be blank"));

        mockMvc.perform(multipart("/api/cases/{caseRecordId}/files", 1L)
                        .file(multipartFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("bad request"))
                .andExpect(jsonPath("$.message").value("Filename must not be blank"));
    }

    @Test
    @DisplayName("listFiles should return ok and file list when files exist")
    void listFiles_shouldReturnOkAndFileList_whenFilesExist() throws Exception {
        CaseFile file1 = new CaseFile();
        file1.setId(10L);
        file1.setOriginalFilename("a.pdf");
        file1.setContentType("application/pdf");
        file1.setSize(100L);
        file1.setUploadedAt(LocalDateTime.of(2026, 4, 16, 12, 0));
        file1.setDocumentNumber(1);
        file1.setDocumentReference("KS26-1-1");

        CaseFile file2 = new CaseFile();
        file2.setId(11L);
        file2.setOriginalFilename("b.txt");
        file2.setContentType("text/plain");
        file2.setSize(200L);
        file2.setUploadedAt(LocalDateTime.of(2026, 4, 16, 12, 5));
        file2.setDocumentNumber(2);
        file2.setDocumentReference("KS26-1-2");
        when(caseFileService.listFiles(1L)).thenReturn(List.of(file1, file2));

        mockMvc.perform(get("/api/cases/{caseRecordId}/files", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].originalFilename").value("a.pdf"))
                .andExpect(jsonPath("$[0].contentType").value("application/pdf"))
                .andExpect(jsonPath("$[0].size").value(100))
                .andExpect(jsonPath("$[0].documentNumber").value(1))
                .andExpect(jsonPath("$[0].documentReference").value("KS26-1-1"))
                .andExpect(jsonPath("$[1].id").value(11))
                .andExpect(jsonPath("$[1].originalFilename").value("b.txt"))
                .andExpect(jsonPath("$[1].contentType").value("text/plain"))
                .andExpect(jsonPath("$[1].size").value(200))
                .andExpect(jsonPath("$[1].documentNumber").value(2))
                .andExpect(jsonPath("$[1].documentReference").value("KS26-1-2"));
    }

    @Test
    @DisplayName("listFiles should return not found when case record does not exist")
    void listFiles_shouldReturnNotFound_whenCaseRecordDoesNotExist() throws Exception {
        when(caseFileService.listFiles(99L))
                .thenThrow(new CaseRecordNotFoundException(99L));

        mockMvc.perform(get("/api/cases/{caseRecordId}/files", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Case record not found: 99"));
    }

    @Test
    @DisplayName("downloadFile should return bytes and headers when file exists")
    void downloadFile_shouldReturnBytesAndHeaders_whenFileExists() throws Exception {
        CaseFile caseFile = new CaseFile();
        caseFile.setId(100L);
        caseFile.setOriginalFilename("document.pdf");
        caseFile.setContentType("application/pdf");
        caseFile.setS3Key("cases/1/uuid-document.pdf");

        when(caseFileService.getCaseFile(1L, 100L)).thenReturn(caseFile);
        when(caseFileService.downloadFile(1L, 100L))
                .thenReturn(new ByteArrayInputStream("hello".getBytes()));

        mockMvc.perform(get("/api/cases/{caseRecordId}/files/{fileId}", 1L, 100L))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("document.pdf")))
                .andExpect(content().contentType("application/pdf"))
                .andExpect(content().bytes("hello".getBytes()));
    }

    @Test
    @DisplayName("downloadFile should return not found when file does not exist")
    void downloadFile_shouldReturnNotFound_whenFileDoesNotExist() throws Exception {
        when(caseFileService.getCaseFile(1L, 999L))
                .thenThrow(new CaseFileNotFoundException(1L, 999L));

        mockMvc.perform(get("/api/cases/{caseRecordId}/files/{fileId}", 1L, 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("File not found for case record. caseRecordId=1, fileId=999"));
    }

    @Test
    @DisplayName("deleteFile should return no content when delete succeeds")
    void deleteFile_shouldReturnNoContent_whenDeleteSucceeds() throws Exception {
        mockMvc.perform(delete("/api/cases/{caseRecordId}/files/{fileId}", 1L, 100L))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("deleteFile should return not found when file does not exist")
    void deleteFile_shouldReturnNotFound_whenFileDoesNotExist() throws Exception {
        doThrow(new CaseFileNotFoundException(1L, 100L))
                .when(caseFileService).deleteFile(1L, 100L);

        mockMvc.perform(delete("/api/cases/{caseRecordId}/files/{fileId}", 1L, 100L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("File not found for case record. caseRecordId=1, fileId=100"));
    }

    @Test
    @DisplayName("uploadFile should return conflict when file key conflict occurs")
    void uploadFile_shouldReturnConflictWhenFileKeyConflictOccurs() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "hello".getBytes()
        );

        when(caseFileService.uploadFile(eq(1L), any()))
                .thenThrow(new FileKeyConflictException("cases/1/conflict-key"));

        mockMvc.perform(multipart("/api/cases/{caseRecordId}/files", 1L)
                        .file(multipartFile))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("conflict"))
                .andExpect(jsonPath("$.message")
                        .value("A file with the same name already exists."));
    }

    @Test
    @DisplayName("uploadFile should return bad request when file is too large")
    void uploadFile_shouldReturnBadRequest_whenFileIsTooLarge() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "big.pdf",
                "application/pdf",
                "hello".getBytes()
        );

        when(caseFileService.uploadFile(eq(1L), any()))
                .thenThrow(new FileTooLargeException((long) (5 * 1024 * 1024)));

        mockMvc.perform(multipart("/api/cases/{caseRecordId}/files", 1L)
                        .file(multipartFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("bad request"))
                .andExpect(jsonPath("$.message").value("Filen är för stor. Maxstorlek är 5 MB."));
    }

}
