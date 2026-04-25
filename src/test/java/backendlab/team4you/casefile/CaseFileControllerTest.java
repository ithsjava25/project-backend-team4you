package backendlab.team4you.casefile;

import backendlab.team4you.audit.AuditLogRepository;
import backendlab.team4you.audit.AuditService;
import backendlab.team4you.common.ConfidentialityLevel;
import backendlab.team4you.exceptions.*;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserRole;
import backendlab.team4you.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CaseFileController.class)
@Import(GlobalRestExceptionHandler.class)
class CaseFileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CaseFileService caseFileService;

    @MockitoBean
    private UserService userService;



    @Test
    @DisplayName("uploadFile should return ok and response body when upload succeeds")
    void uploadFile_shouldReturnOkAndResponseBody_whenUploadSucceeds() throws Exception {
        UserEntity currentUser = mock(UserEntity.class);

        CaseFile savedFile = new CaseFile();
        savedFile.setId(10L);
        savedFile.setOriginalFilename("test.pdf");
        savedFile.setContentType("application/pdf");
        savedFile.setSize(123L);
        savedFile.setUploadedAt(LocalDateTime.of(2026, 4, 16, 12, 0));
        savedFile.setDocumentNumber(1);
        savedFile.setDocumentReference("KS26-1-1");
        savedFile.setConfidentialityLevel(ConfidentialityLevel.OPEN);

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "hello".getBytes()
        );

        when(userService.getCurrentUser(any())).thenReturn(currentUser);
        when(caseFileService.uploadFile(eq(1L), any(), eq(ConfidentialityLevel.OPEN), eq(currentUser)))
                .thenReturn(savedFile);

        mockMvc.perform(multipart("/api/cases/{caseRecordId}/files", 1L)
                        .file(multipartFile)
                        .param("confidentialityLevel", "OPEN")
                        .principal(() -> "dev"))
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
        UserEntity currentUser = mock(UserEntity.class);

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "hello".getBytes()
        );

        when(userService.getCurrentUser(any())).thenReturn(currentUser);
        when(caseFileService.uploadFile(eq(99L), any(), eq(ConfidentialityLevel.OPEN), eq(currentUser)))
                .thenThrow(new CaseRecordNotFoundException(99L));

        mockMvc.perform(multipart("/api/cases/{caseRecordId}/files", 99L)
                        .file(multipartFile)
                        .param("confidentialityLevel", "OPEN")
                        .principal(() -> "dev"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("not found"))
                .andExpect(jsonPath("$.message").value("Case record not found: 99"));
    }

    @Test
    @DisplayName("uploadFile should return bad request when filename is invalid")
    void uploadFile_shouldReturnBadRequest_whenFilenameIsInvalid() throws Exception {
        UserEntity currentUser = mock(UserEntity.class);

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "",
                "application/pdf",
                "hello".getBytes()
        );

        when(userService.getCurrentUser(any())).thenReturn(currentUser);
        when(caseFileService.uploadFile(eq(1L), any(), eq(ConfidentialityLevel.OPEN), eq(currentUser)))
                .thenThrow(new InvalidFileNameException("Filnamn måste anges."));

        mockMvc.perform(multipart("/api/cases/{caseRecordId}/files", 1L)
                        .file(multipartFile)
                        .param("confidentialityLevel", "OPEN")
                        .principal(() -> "dev"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("bad request"))
                .andExpect(jsonPath("$.message").value("Filnamn måste anges."));
    }

    @Test
    @DisplayName("listFiles should return ok and viewer-safe file list when files exist")
    void listFiles_shouldReturnOkAndViewerSafeFileList_whenFilesExist() throws Exception {
        UserEntity currentUser = mock(UserEntity.class);

        CaseFileListItemDto file1 = new CaseFileListItemDto(
                10L,
                "KS26-1-1",
                "a.pdf",
                false,
                true
        );

        CaseFileListItemDto file2 = new CaseFileListItemDto(
                11L,
                "KS26-1-2",
                "Sekretess",
                true,
                false
        );

        when(userService.getCurrentUser(any())).thenReturn(currentUser);
        when(caseFileService.listFileItemsForViewer(1L, currentUser)).thenReturn(List.of(file1, file2));

        mockMvc.perform(get("/api/cases/{caseRecordId}/files", 1L)
                        .principal(() -> "dev"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].documentReference").value("KS26-1-1"))
                .andExpect(jsonPath("$[0].displayName").value("a.pdf"))
                .andExpect(jsonPath("$[0].confidential").value(false))
                .andExpect(jsonPath("$[0].canDownload").value(true))
                .andExpect(jsonPath("$[1].id").value(11))
                .andExpect(jsonPath("$[1].documentReference").value("KS26-1-2"))
                .andExpect(jsonPath("$[1].displayName").value("Sekretess"))
                .andExpect(jsonPath("$[1].confidential").value(true))
                .andExpect(jsonPath("$[1].canDownload").value(false));
    }

    @Test
    @DisplayName("listFiles should return not found when case record does not exist")
    void listFiles_shouldReturnNotFound_whenCaseRecordDoesNotExist() throws Exception {
        UserEntity currentUser = mock(UserEntity.class);

        when(userService.getCurrentUser(any())).thenReturn(currentUser);
        when(caseFileService.listFileItemsForViewer(99L, currentUser))
                .thenThrow(new CaseRecordNotFoundException(99L));

        mockMvc.perform(get("/api/cases/{caseRecordId}/files", 99L)
                        .principal(() -> "dev"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Case record not found: 99"));
    }

    @Test
    @DisplayName("downloadFile should return bytes and headers when file exists")
    void downloadFile_shouldReturnBytesAndHeaders_whenFileExists() throws Exception {
        UserEntity user = new UserEntity();
        user.setName("dev");
        user.setRole(UserRole.USER);
        user.setId(new Bytes("dev".getBytes()));

        CaseFile caseFile = new CaseFile();
        caseFile.setId(100L);
        caseFile.setOriginalFilename("document.pdf");
        caseFile.setContentType("application/pdf");
        caseFile.setS3Key("cases/1/uuid-document.pdf");
        caseFile.setConfidentialityLevel(ConfidentialityLevel.OPEN);

        when(userService.getCurrentUser(any())).thenReturn(user);
        when(caseFileService.getCaseFileForViewer(1L, 100L, user)).thenReturn(caseFile);
        when(caseFileService.downloadFile(1L, 100L, user))
                .thenReturn(new ByteArrayInputStream("hello".getBytes()));

        MvcResult mvcResult = mockMvc.perform(get("/api/cases/{caseRecordId}/files/{fileId}", 1L, 100L)
                        .principal(() -> "dev"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("document.pdf")))
                .andExpect(content().contentType("application/pdf"))
                .andExpect(content().bytes("hello".getBytes()));
    }

    @Test
    @DisplayName("downloadFile should return not found when file does not exist")
    void downloadFile_shouldReturnNotFound_whenFileDoesNotExist() throws Exception {
        UserEntity user = new UserEntity();
        user.setName("dev");
        user.setRole(UserRole.USER);
        user.setId(new Bytes("dev".getBytes()));

        when(userService.getCurrentUser(any())).thenReturn(user);
        when(caseFileService.getCaseFileForViewer(1L, 999L, user))
                .thenThrow(new CaseFileNotFoundException(1L, 999L));

        mockMvc.perform(get("/api/cases/{caseRecordId}/files/{fileId}", 1L, 999L)
                        .principal(() -> "dev"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("File not found for case record. caseRecordId=1, fileId=999"));
    }

    @Test
    @DisplayName("deleteFile should return no content when delete succeeds")
    void deleteFile_shouldReturnNoContent_whenDeleteSucceeds() throws Exception {
        UserEntity currentUser = mock(UserEntity.class);

        when(userService.getCurrentUser(any())).thenReturn(currentUser);

        mockMvc.perform(delete("/api/cases/{caseRecordId}/files/{fileId}", 1L, 100L)
                        .principal(() -> "dev"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("deleteFile should return not found when file does not exist")
    void deleteFile_shouldReturnNotFound_whenFileDoesNotExist() throws Exception {
        UserEntity currentUser = mock(UserEntity.class);

        when(userService.getCurrentUser(any())).thenReturn(currentUser);
        doThrow(new CaseFileNotFoundException(1L, 100L))
                .when(caseFileService).deleteFile(1L, 100L, currentUser);

        mockMvc.perform(delete("/api/cases/{caseRecordId}/files/{fileId}", 1L, 100L)
                        .principal(() -> "dev"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("File not found for case record. caseRecordId=1, fileId=100"));
    }

    @Test
    @DisplayName("uploadFile should return conflict when file key conflict occurs")
    void uploadFile_shouldReturnConflictWhenFileKeyConflictOccurs() throws Exception {
        UserEntity currentUser = mock(UserEntity.class);

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "hello".getBytes()
        );

        when(userService.getCurrentUser(any())).thenReturn(currentUser);
        when(caseFileService.uploadFile(eq(1L), any(), eq(ConfidentialityLevel.OPEN), eq(currentUser)))
                .thenThrow(new FileKeyConflictException("cases/1/conflict-key"));

        mockMvc.perform(multipart("/api/cases/{caseRecordId}/files", 1L)
                        .file(multipartFile)
                        .param("confidentialityLevel", "OPEN")
                        .principal(() -> "dev"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("conflict"))
                .andExpect(jsonPath("$.message")
                        .value("A file with the same name already exists."));
    }

    @Test
    @DisplayName("uploadFile should return content too large when file is too large")
    void uploadFile_shouldReturnContentTooLarge_whenFileIsTooLarge() throws Exception {
        UserEntity currentUser = mock(UserEntity.class);

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "big.pdf",
                "application/pdf",
                "hello".getBytes()
        );

        when(userService.getCurrentUser(any())).thenReturn(currentUser);
        when(caseFileService.uploadFile(eq(1L), any(), eq(ConfidentialityLevel.OPEN), eq(currentUser)))
                .thenThrow(new FileTooLargeException((long) (5 * 1024 * 1024)));

        mockMvc.perform(multipart("/api/cases/{caseRecordId}/files", 1L)
                        .file(multipartFile)
                        .param("confidentialityLevel", "OPEN")
                        .principal(() -> "dev"))
                .andExpect(status().isContentTooLarge())
                .andExpect(jsonPath("$.status").value(413))
                .andExpect(jsonPath("$.error").value("content too large"))
                .andExpect(jsonPath("$.message").value("Filen är för stor. Maxstorlek är 5 MB."));
    }


    @Test
    @DisplayName("downloadFile should return forbidden when user lacks access to confidential file")
    void downloadFile_shouldReturnForbidden_whenUserLacksAccessToConfidentialFile() throws Exception {
        UserEntity user = new UserEntity();
        user.setName("dev");
        user.setRole(UserRole.USER);
        user.setId(Bytes.fromBase64("ZGV2"));

        when(userService.getCurrentUser(any())).thenReturn(user);
        when(caseFileService.getCaseFileForViewer(1L, 100L, user))
                .thenThrow(new org.springframework.security.access.AccessDeniedException(
                        "Du har inte behörighet att öppna denna fil."
                ));

        mockMvc.perform(get("/api/cases/{caseRecordId}/files/{fileId}", 1L, 100L)
                        .principal(() -> "dev"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("forbidden"))
                .andExpect(jsonPath("$.message").value("Du har inte behörighet att öppna denna fil."));
    }

    @Test
    @DisplayName("downloadFile should return file when user has access to confidential file")
    void downloadFile_shouldReturnFile_whenUserHasAccessToConfidentialFile() throws Exception {
        UserEntity admin = new UserEntity();
        admin.setName("dev");
        admin.setRole(UserRole.ADMIN);
        admin.setId(new Bytes("dev".getBytes()));

        CaseFile caseFile = new CaseFile();
        caseFile.setId(100L);
        caseFile.setOriginalFilename("secret.pdf");
        caseFile.setContentType("application/pdf");
        caseFile.setS3Key("cases/1/secret.pdf");
        caseFile.setConfidentialityLevel(ConfidentialityLevel.CONFIDENTIAL);

        when(userService.getCurrentUser(any())).thenReturn(admin);
        when(caseFileService.getCaseFileForViewer(1L, 100L, admin)).thenReturn(caseFile);
        when(caseFileService.downloadFile(1L, 100L, admin))
                .thenReturn(new ByteArrayInputStream("hello".getBytes()));

        MvcResult mvcResult = mockMvc.perform(get("/api/cases/{caseRecordId}/files/{fileId}", 1L, 100L)
                        .principal(() -> "dev"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("secret.pdf")))
                .andExpect(content().contentType("application/pdf"))
                .andExpect(content().bytes("hello".getBytes()));
    }
}
