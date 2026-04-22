package backendlab.team4you.casefile;

import backendlab.team4you.casefile.access.CaseFileAccessService;
import backendlab.team4you.caserecord.CaseRecord;
import backendlab.team4you.caserecord.CaseRecordRepository;
import backendlab.team4you.common.ConfidentialityLevel;
import backendlab.team4you.exceptions.*;
import backendlab.team4you.s3.S3Service;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseFileServiceTest {

    @Mock
    private CaseRecordRepository caseRecordRepository;

    @Mock
    private CaseFileRepository caseFileRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private CaseFileAccessService caseFileAccessService;

    @InjectMocks
    private CaseFileService caseFileService;

    private CaseRecord caseRecord;
    private UserEntity actor;
    private UserEntity viewer;

    @BeforeEach
    void setUp() {
        caseRecord = new CaseRecord();
        caseRecord.setId(1L);
        caseRecord.setCaseNumber("KS26-1");

        actor = new UserEntity();
        actor.setName("actor");
        actor.setRole(UserRole.USER);

        viewer = new UserEntity();
        viewer.setName("viewer");
        viewer.setRole(UserRole.USER);
    }

    @Test
    @DisplayName("uploadFile should upload to s3 and save metadata when case record exists")
    void uploadFile_shouldUploadToS3AndSaveMetadata_whenCaseRecordExists() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "hello world".getBytes()
        );

        when(caseFileAccessService.canUploadFile(actor, 1L, ConfidentialityLevel.OPEN)).thenReturn(true);
        when(caseRecordRepository.findByIdWithLock(1L)).thenReturn(Optional.of(caseRecord));
        when(caseFileRepository.findTopByCaseRecordIdOrderByDocumentNumberDesc(1L)).thenReturn(Optional.empty());
        when(caseFileRepository.saveAndFlush(any(CaseFile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CaseFile result = caseFileService.uploadFile(1L, file, ConfidentialityLevel.OPEN, actor);

        assertThat(result).isNotNull();
        assertThat(result.getCaseRecord()).isEqualTo(caseRecord);
        assertThat(result.getOriginalFilename()).isEqualTo("test.pdf");
        assertThat(result.getContentType()).isEqualTo("application/pdf");
        assertThat(result.getSize()).isEqualTo(file.getSize());
        assertThat(result.getUploadedAt()).isNotNull();
        assertThat(result.getS3Key()).isNotBlank();
        assertThat(result.getS3Key()).contains("cases/1/");
        assertThat(result.getS3Key()).endsWith("-test.pdf");
        assertThat(result.getDocumentNumber()).isEqualTo(1);
        assertThat(result.getDocumentReference()).isEqualTo("KS26-1-1");

        verify(s3Service).uploadFileIfAbsent(
                startsWith("cases/1/"),
                eq(file.getBytes()),
                eq("application/pdf")
        );
        verify(caseFileRepository).saveAndFlush(any(CaseFile.class));
    }

    @Test
    @DisplayName("uploadFile should throw AccessDeniedException when actor lacks upload permission")
    void uploadFile_shouldThrowAccessDeniedException_whenActorLacksUploadPermission() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "hello".getBytes()
        );

        when(caseFileAccessService.canUploadFile(actor, 1L, ConfidentialityLevel.OPEN)).thenReturn(false);

        assertThatThrownBy(() -> caseFileService.uploadFile(1L, file, ConfidentialityLevel.OPEN, actor))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Du har inte behörighet att ladda upp denna fil.");

        verifyNoInteractions(caseRecordRepository, caseFileRepository, s3Service);
    }

    @Test
    @DisplayName("uploadFile should throw CaseRecordNotFoundException when case record does not exist")
    void uploadFile_shouldThrowCaseRecordNotFoundException_whenCaseRecordDoesNotExist() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "hello world".getBytes()
        );

        when(caseFileAccessService.canUploadFile(actor, 99L, ConfidentialityLevel.OPEN)).thenReturn(true);
        when(caseRecordRepository.findByIdWithLock(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseFileService.uploadFile(99L, file, ConfidentialityLevel.OPEN, actor))
                .isInstanceOf(CaseRecordNotFoundException.class)
                .hasMessage("Case record not found: 99");

        verify(s3Service, never()).uploadFileIfAbsent(anyString(), any(), anyString());
        verify(caseFileRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("uploadFile should throw InvalidFileNameException when filename is blank")
    void uploadFile_shouldThrowInvalidFileNameException_whenFilenameIsBlank() {
        MultipartFile file = mock(MultipartFile.class);

        when(caseFileAccessService.canUploadFile(actor, 1L, ConfidentialityLevel.OPEN)).thenReturn(true);
        when(caseRecordRepository.findByIdWithLock(1L)).thenReturn(Optional.of(caseRecord));
        when(file.getSize()).thenReturn(10L);
        when(file.getOriginalFilename()).thenReturn(" ");

        assertThatThrownBy(() -> caseFileService.uploadFile(1L, file, ConfidentialityLevel.OPEN, actor))
                .isInstanceOf(InvalidFileNameException.class)
                .hasMessage("Filnamn måste anges.");

        verify(s3Service, never()).uploadFileIfAbsent(anyString(), any(), anyString());
        verify(caseFileRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("uploadFile should default content type when multipart file content type is null")
    void uploadFile_shouldDefaultContentType_whenMultipartFileContentTypeIsNull() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.txt",
                null,
                "abc".getBytes()
        );

        when(caseFileAccessService.canUploadFile(actor, 1L, ConfidentialityLevel.OPEN)).thenReturn(true);
        when(caseRecordRepository.findByIdWithLock(1L)).thenReturn(Optional.of(caseRecord));
        when(caseFileRepository.findTopByCaseRecordIdOrderByDocumentNumberDesc(1L)).thenReturn(Optional.empty());
        when(caseFileRepository.saveAndFlush(any(CaseFile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CaseFile result = caseFileService.uploadFile(1L, file, ConfidentialityLevel.OPEN, actor);

        assertThat(result.getContentType()).isEqualTo("application/octet-stream");

        verify(s3Service).uploadFileIfAbsent(
                startsWith("cases/1/"),
                eq(file.getBytes()),
                eq("application/octet-stream")
        );
    }

    @Test
    @DisplayName("listFiles should return files for case record when case record exists")
    void listFiles_shouldReturnFiles_whenCaseRecordExists() {
        CaseFile file1 = new CaseFile();
        file1.setId(10L);
        file1.setOriginalFilename("a.pdf");

        CaseFile file2 = new CaseFile();
        file2.setId(11L);
        file2.setOriginalFilename("b.pdf");

        when(caseRecordRepository.existsById(1L)).thenReturn(true);
        when(caseFileRepository.findByCaseRecordIdOrderByDocumentNumberAsc(1L)).thenReturn(List.of(file1, file2));

        List<CaseFile> result = caseFileService.listFiles(1L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CaseFile::getOriginalFilename)
                .containsExactly("a.pdf", "b.pdf");

        verify(caseRecordRepository).existsById(1L);
        verify(caseFileRepository).findByCaseRecordIdOrderByDocumentNumberAsc(1L);
    }

    @Test
    @DisplayName("listFiles should throw CaseRecordNotFoundException when case record does not exist")
    void listFiles_shouldThrowCaseRecordNotFoundException_whenCaseRecordDoesNotExist() {
        when(caseRecordRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> caseFileService.listFiles(1L))
                .isInstanceOf(CaseRecordNotFoundException.class)
                .hasMessage("Case record not found: 1");

        verify(caseFileRepository, never()).findByCaseRecordIdOrderByDocumentNumberAsc(anyLong());
    }

    @Test
    @DisplayName("getCaseFile should return file when file belongs to case record")
    void getCaseFile_shouldReturnFile_whenFileBelongsToCaseRecord() {
        CaseFile caseFile = new CaseFile();
        caseFile.setId(100L);
        caseFile.setOriginalFilename("contract.pdf");

        when(caseFileRepository.findByIdAndCaseRecordId(100L, 1L))
                .thenReturn(Optional.of(caseFile));

        CaseFile result = caseFileService.getCaseFile(1L, 100L);

        assertThat(result).isEqualTo(caseFile);
        verify(caseFileRepository).findByIdAndCaseRecordId(100L, 1L);
    }

    @Test
    @DisplayName("getCaseFile should throw CaseFileNotFoundException when file does not belong to case record")
    void getCaseFile_shouldThrowCaseFileNotFoundException_whenFileDoesNotExistForCaseRecord() {
        when(caseFileRepository.findByIdAndCaseRecordId(100L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseFileService.getCaseFile(1L, 100L))
                .isInstanceOf(CaseFileNotFoundException.class)
                .hasMessage("File not found for case record. caseRecordId=1, fileId=100");
    }

    @Test
    @DisplayName("getCaseFileForViewer should return file when viewer has access")
    void getCaseFileForViewer_shouldReturnFile_whenViewerHasAccess() {
        CaseFile caseFile = new CaseFile();
        caseFile.setId(100L);
        caseFile.setCaseRecord(caseRecord);
        caseFile.setConfidentialityLevel(ConfidentialityLevel.OPEN);

        when(caseFileRepository.findByIdAndCaseRecordId(100L, 1L)).thenReturn(Optional.of(caseFile));
        when(caseFileAccessService.canViewFile(viewer, caseFile)).thenReturn(true);

        CaseFile result = caseFileService.getCaseFileForViewer(1L, 100L, viewer);

        assertThat(result).isEqualTo(caseFile);
    }

    @Test
    @DisplayName("getCaseFileForViewer should throw AccessDeniedException when viewer lacks access")
    void getCaseFileForViewer_shouldThrowAccessDeniedException_whenViewerLacksAccess() {
        CaseFile caseFile = new CaseFile();
        caseFile.setId(100L);
        caseFile.setCaseRecord(caseRecord);
        caseFile.setConfidentialityLevel(ConfidentialityLevel.CONFIDENTIAL);

        when(caseFileRepository.findByIdAndCaseRecordId(100L, 1L)).thenReturn(Optional.of(caseFile));
        when(caseFileAccessService.canViewFile(viewer, caseFile)).thenReturn(false);

        assertThatThrownBy(() -> caseFileService.getCaseFileForViewer(1L, 100L, viewer))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Du har inte behörighet att öppna denna fil.");
    }

    @Test
    @DisplayName("downloadFile should return input stream from s3 when viewer has access")
    void downloadFile_shouldReturnInputStreamFromS3_whenViewerHasAccess() throws IOException {
        CaseFile caseFile = new CaseFile();
        caseFile.setId(100L);
        caseFile.setCaseRecord(caseRecord);
        caseFile.setS3Key("cases/1/uuid-test.pdf");
        caseFile.setConfidentialityLevel(ConfidentialityLevel.OPEN);

        InputStream inputStream = new ByteArrayInputStream("hello".getBytes());

        when(caseFileRepository.findByIdAndCaseRecordId(100L, 1L))
                .thenReturn(Optional.of(caseFile));
        when(caseFileAccessService.canViewFile(viewer, caseFile)).thenReturn(true);
        when(s3Service.downloadFile("cases/1/uuid-test.pdf"))
                .thenReturn(inputStream);

        InputStream result = caseFileService.downloadFile(1L, 100L, viewer);

        assertThat(result.readAllBytes()).isEqualTo("hello".getBytes());
        verify(s3Service).downloadFile("cases/1/uuid-test.pdf");
    }

    @Test
    @DisplayName("deleteFile should delete from s3 and repository when actor has permission")
    void deleteFile_shouldDeleteFromS3AndRepository_whenActorHasPermission() {
        CaseFile caseFile = new CaseFile();
        caseFile.setId(100L);
        caseFile.setCaseRecord(caseRecord);
        caseFile.setS3Key("cases/1/uuid-test.pdf");

        when(caseFileRepository.findByIdAndCaseRecordId(100L, 1L))
                .thenReturn(Optional.of(caseFile));
        when(caseFileAccessService.canDeleteFile(actor, caseFile)).thenReturn(true);

        caseFileService.deleteFile(1L, 100L, actor);

        verify(caseFileRepository).delete(caseFile);
        verify(s3Service).deleteFile("cases/1/uuid-test.pdf");
    }

    @Test
    @DisplayName("deleteFile should throw AccessDeniedException when actor lacks permission")
    void deleteFile_shouldThrowAccessDeniedException_whenActorLacksPermission() {
        CaseFile caseFile = new CaseFile();
        caseFile.setId(100L);
        caseFile.setCaseRecord(caseRecord);
        caseFile.setS3Key("cases/1/uuid-test.pdf");

        when(caseFileRepository.findByIdAndCaseRecordId(100L, 1L))
                .thenReturn(Optional.of(caseFile));
        when(caseFileAccessService.canDeleteFile(actor, caseFile)).thenReturn(false);

        assertThatThrownBy(() -> caseFileService.deleteFile(1L, 100L, actor))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Du har inte behörighet att radera denna fil.");

        verify(caseFileRepository, never()).delete(any());
        verify(s3Service, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("uploadFile should sanitize filename when building s3 key")
    void uploadFile_shouldSanitizeFilename_whenBuildingS3Key() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "my file(1).pdf",
                "application/pdf",
                "hello".getBytes()
        );

        when(caseFileAccessService.canUploadFile(actor, 1L, ConfidentialityLevel.OPEN)).thenReturn(true);
        when(caseRecordRepository.findByIdWithLock(1L)).thenReturn(Optional.of(caseRecord));
        when(caseFileRepository.findTopByCaseRecordIdOrderByDocumentNumberDesc(1L)).thenReturn(Optional.empty());
        when(caseFileRepository.saveAndFlush(any(CaseFile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        caseFileService.uploadFile(1L, file, ConfidentialityLevel.OPEN, actor);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(s3Service).uploadFileIfAbsent(keyCaptor.capture(), any(byte[].class), eq("application/pdf"));

        String generatedKey = keyCaptor.getValue();
        assertThat(generatedKey).contains("cases/1/");
        assertThat(generatedKey).endsWith("-my_file_1_.pdf");
    }

    @Test
    @DisplayName("uploadFile should delete uploaded object when repository save fails")
    void uploadFile_shouldDeleteUploadedObjectWhenRepositorySaveFails() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "hello".getBytes()
        );

        when(caseFileAccessService.canUploadFile(actor, 1L, ConfidentialityLevel.OPEN)).thenReturn(true);
        when(caseRecordRepository.findByIdWithLock(1L)).thenReturn(Optional.of(caseRecord));
        when(caseFileRepository.findTopByCaseRecordIdOrderByDocumentNumberDesc(1L)).thenReturn(Optional.empty());
        doNothing().when(s3Service).uploadFileIfAbsent(anyString(), any(byte[].class), eq("application/pdf"));
        when(caseFileRepository.saveAndFlush(any(CaseFile.class)))
                .thenThrow(new DataIntegrityViolationException("database failure"));

        assertThatThrownBy(() -> caseFileService.uploadFile(1L, file, ConfidentialityLevel.OPEN, actor))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("database failure");

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);

        verify(s3Service).uploadFileIfAbsent(
                keyCaptor.capture(),
                eq(file.getBytes()),
                eq("application/pdf")
        );
        verify(s3Service).deleteFile(keyCaptor.getValue());
        verify(caseFileRepository).saveAndFlush(any(CaseFile.class));
    }

    @Test
    @DisplayName("uploadFile should rethrow original exception when cleanup delete fails")
    void uploadFile_shouldRethrowOriginalExceptionWhenCleanupDeleteFails() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "hello".getBytes()
        );

        when(caseFileAccessService.canUploadFile(actor, 1L, ConfidentialityLevel.OPEN)).thenReturn(true);
        when(caseRecordRepository.findByIdWithLock(1L)).thenReturn(Optional.of(caseRecord));
        when(caseFileRepository.findTopByCaseRecordIdOrderByDocumentNumberDesc(1L)).thenReturn(Optional.empty());
        doNothing().when(s3Service).uploadFileIfAbsent(anyString(), any(byte[].class), eq("application/pdf"));

        DataIntegrityViolationException originalException =
                new DataIntegrityViolationException("database failure");

        when(caseFileRepository.saveAndFlush(any(CaseFile.class))).thenThrow(originalException);
        doThrow(new RuntimeException("cleanup failed"))
                .when(s3Service)
                .deleteFile(anyString());

        assertThatThrownBy(() -> caseFileService.uploadFile(1L, file, ConfidentialityLevel.OPEN, actor))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("database failure");

        verify(s3Service).uploadFileIfAbsent(anyString(), eq(file.getBytes()), eq("application/pdf"));
        verify(s3Service).deleteFile(anyString());
        verify(caseFileRepository).saveAndFlush(any(CaseFile.class));
    }

    @Test
    @DisplayName("uploadFile should throw FileKeyConflictException when s3 key already exists")
    void uploadFile_shouldThrowFileKeyConflictExceptionWhenS3KeyAlreadyExists() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "hello".getBytes()
        );

        when(caseFileAccessService.canUploadFile(actor, 1L, ConfidentialityLevel.OPEN)).thenReturn(true);
        when(caseRecordRepository.findByIdWithLock(1L)).thenReturn(Optional.of(caseRecord));
        when(caseFileRepository.findTopByCaseRecordIdOrderByDocumentNumberDesc(1L)).thenReturn(Optional.empty());

        doThrow(new FileKeyConflictException(""))
                .when(s3Service)
                .uploadFileIfAbsent(anyString(), any(byte[].class), eq("application/pdf"));

        assertThatThrownBy(() -> caseFileService.uploadFile(1L, file, ConfidentialityLevel.OPEN, actor))
                .isInstanceOf(FileKeyConflictException.class)
                .hasMessageContaining("A file with the same name already exists.");

        verify(caseFileRepository, never()).saveAndFlush(any());
        verify(s3Service, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("deleteFile should delete metadata before attempting s3 delete")
    void deleteFile_shouldDeleteMetadataBeforeAttemptingS3Delete() {
        CaseFile caseFile = new CaseFile();
        caseFile.setId(100L);
        caseFile.setCaseRecord(caseRecord);
        caseFile.setS3Key("cases/1/uuid-test.pdf");

        when(caseFileRepository.findByIdAndCaseRecordId(100L, 1L))
                .thenReturn(Optional.of(caseFile));
        when(caseFileAccessService.canDeleteFile(actor, caseFile)).thenReturn(true);

        doThrow(new RuntimeException("s3 delete failed"))
                .when(s3Service)
                .deleteFile("cases/1/uuid-test.pdf");

        assertThatThrownBy(() -> caseFileService.deleteFile(1L, 100L, actor))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("s3 delete failed");

        verify(caseFileRepository).delete(caseFile);
        verify(s3Service).deleteFile("cases/1/uuid-test.pdf");
    }

    @DisplayName("uploadFile should throw FileTooLargeException when file is too large")
    void uploadFile_shouldThrowFileTooLargeException_whenFileTooLarge() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "big.pdf",
                "application/pdf",
                new byte[6 * 1024 * 1024]
        );

        when(caseFileAccessService.canUploadFile(actor, 1L, ConfidentialityLevel.OPEN)).thenReturn(true);

        assertThatThrownBy(() -> caseFileService.uploadFile(1L, file, ConfidentialityLevel.OPEN, actor))
                .isInstanceOf(FileTooLargeException.class)
                .hasMessageContaining("Filen är för stor.");
    }

    @Test
    @DisplayName("uploadFile should not access repositories or s3 when file is too large")
    void uploadFile_shouldNotAccessRepositoriesOrS3_whenFileTooLarge() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "big.pdf",
                "application/pdf",
                new byte[6 * 1024 * 1024]
        );

        when(caseFileAccessService.canUploadFile(actor, 1L, ConfidentialityLevel.OPEN)).thenReturn(true);

        assertThatThrownBy(() -> caseFileService.uploadFile(1L, file, ConfidentialityLevel.OPEN, actor))
                .isInstanceOf(FileTooLargeException.class)
                .hasMessageContaining("Filen är för stor.");

        verifyNoInteractions(caseRecordRepository, caseFileRepository, s3Service);
    }

    @Test
    @DisplayName("uploadFile should assign document number 1 and correct reference for first file in case")
    void uploadFile_shouldAssignDocumentNumber1AndReference_forFirstFileInCase() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "hello".getBytes()
        );

        when(caseFileAccessService.canUploadFile(actor, 1L, ConfidentialityLevel.OPEN)).thenReturn(true);
        when(caseRecordRepository.findByIdWithLock(1L)).thenReturn(Optional.of(caseRecord));
        when(caseFileRepository.findTopByCaseRecordIdOrderByDocumentNumberDesc(1L)).thenReturn(Optional.empty());
        when(caseFileRepository.saveAndFlush(any(CaseFile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CaseFile result = caseFileService.uploadFile(1L, file, ConfidentialityLevel.OPEN, actor);

        assertThat(result.getDocumentNumber()).isEqualTo(1);
        assertThat(result.getDocumentReference()).isEqualTo("KS26-1-1");
    }

    @Test
    @DisplayName("uploadFile should assign next document number and reference for subsequent file")
    void uploadFile_shouldAssignNextDocumentNumberAndReference_forSubsequentFile() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "hello".getBytes()
        );

        CaseFile existingFile = new CaseFile();
        existingFile.setDocumentNumber(1);

        when(caseFileAccessService.canUploadFile(actor, 1L, ConfidentialityLevel.OPEN)).thenReturn(true);
        when(caseRecordRepository.findByIdWithLock(1L)).thenReturn(Optional.of(caseRecord));
        when(caseFileRepository.findTopByCaseRecordIdOrderByDocumentNumberDesc(1L))
                .thenReturn(Optional.of(existingFile));
        when(caseFileRepository.saveAndFlush(any(CaseFile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CaseFile result = caseFileService.uploadFile(1L, file, ConfidentialityLevel.OPEN, actor);

        assertThat(result.getDocumentNumber()).isEqualTo(2);
        assertThat(result.getDocumentReference()).isEqualTo("KS26-1-2");
    }

    @Test
    @DisplayName("listFileItemsForViewer should hide confidential filename for unauthorized user")
    void listFileItemsForViewer_shouldHideConfidentialFilename_forUnauthorizedUser() {
        CaseFile confidentialFile = new CaseFile();
        confidentialFile.setId(100L);
        confidentialFile.setCaseRecord(caseRecord);
        confidentialFile.setDocumentNumber(1);
        confidentialFile.setDocumentReference("KS26-1-1");
        confidentialFile.setOriginalFilename("hemligt-avtal.pdf");
        confidentialFile.setConfidentialityLevel(ConfidentialityLevel.CONFIDENTIAL);

        when(caseRecordRepository.existsById(1L)).thenReturn(true);
        when(caseFileRepository.findByCaseRecordIdOrderByDocumentNumberAsc(1L))
                .thenReturn(List.of(confidentialFile));
        when(caseFileAccessService.canViewFile(viewer, confidentialFile)).thenReturn(false);

        List<CaseFileListItemDto> result = caseFileService.listFileItemsForViewer(1L, viewer);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).displayName()).isEqualTo("Sekretess");
        assertThat(result.get(0).canDownload()).isFalse();
        assertThat(result.get(0).confidential()).isTrue();
        assertThat(result.get(0).documentReference()).isEqualTo("KS26-1-1");
    }

    @Test
    @DisplayName("listFileItemsForViewer should show confidential filename for authorized user")
    void listFileItemsForViewer_shouldShowConfidentialFilename_forAuthorizedUser() {
        UserEntity admin = new UserEntity();
        admin.setRole(UserRole.ADMIN);

        CaseFile confidentialFile = new CaseFile();
        confidentialFile.setId(100L);
        confidentialFile.setCaseRecord(caseRecord);
        confidentialFile.setDocumentNumber(1);
        confidentialFile.setDocumentReference("KS26-1-1");
        confidentialFile.setOriginalFilename("hemligt-avtal.pdf");
        confidentialFile.setConfidentialityLevel(ConfidentialityLevel.CONFIDENTIAL);

        when(caseRecordRepository.existsById(1L)).thenReturn(true);
        when(caseFileRepository.findByCaseRecordIdOrderByDocumentNumberAsc(1L))
                .thenReturn(List.of(confidentialFile));
        when(caseFileAccessService.canViewFile(admin, confidentialFile)).thenReturn(true);

        List<CaseFileListItemDto> result = caseFileService.listFileItemsForViewer(1L, admin);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).displayName()).isEqualTo("hemligt-avtal.pdf");
        assertThat(result.get(0).canDownload()).isTrue();
        assertThat(result.get(0).confidential()).isTrue();
    }

    @Test
    @DisplayName("listFileItemsForViewer should show open file normally")
    void listFileItemsForViewer_shouldShowOpenFileNormally() {
        CaseFile openFile = new CaseFile();
        openFile.setId(101L);
        openFile.setCaseRecord(caseRecord);
        openFile.setDocumentNumber(1);
        openFile.setDocumentReference("KS26-1-1");
        openFile.setOriginalFilename("offentlig.pdf");
        openFile.setConfidentialityLevel(ConfidentialityLevel.OPEN);

        when(caseRecordRepository.existsById(1L)).thenReturn(true);
        when(caseFileRepository.findByCaseRecordIdOrderByDocumentNumberAsc(1L))
                .thenReturn(List.of(openFile));
        when(caseFileAccessService.canViewFile(viewer, openFile)).thenReturn(true);

        List<CaseFileListItemDto> result = caseFileService.listFileItemsForViewer(1L, viewer);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).displayName()).isEqualTo("offentlig.pdf");
        assertThat(result.get(0).canDownload()).isTrue();
        assertThat(result.get(0).confidential()).isFalse();
    }
}