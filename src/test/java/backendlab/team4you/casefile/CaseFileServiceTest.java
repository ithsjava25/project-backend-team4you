package backendlab.team4you.casefile;

import backendlab.team4you.caserecord.CaseRecord;
import backendlab.team4you.caserecord.CaseRecordRepository;
import backendlab.team4you.exceptions.CaseFileNotFoundException;
import backendlab.team4you.exceptions.CaseRecordNotFoundException;
import backendlab.team4you.exceptions.InvalidFileNameException;
import backendlab.team4you.s3.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
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

    @InjectMocks
    private CaseFileService caseFileService;

    private CaseRecord caseRecord;

    @BeforeEach
    void setUp() {
        caseRecord = new CaseRecord();
        caseRecord.setId(1L);
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

        when(caseRecordRepository.findById(1L)).thenReturn(Optional.of(caseRecord));
        when(caseFileRepository.save(any(CaseFile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CaseFile result = caseFileService.uploadFile(1L, file);

        assertThat(result).isNotNull();
        assertThat(result.getCaseRecord()).isEqualTo(caseRecord);
        assertThat(result.getOriginalFilename()).isEqualTo("test.pdf");
        assertThat(result.getContentType()).isEqualTo("application/pdf");
        assertThat(result.getSize()).isEqualTo(file.getSize());
        assertThat(result.getUploadedAt()).isNotNull();
        assertThat(result.getS3Key()).isNotBlank();
        assertThat(result.getS3Key()).contains("cases/1/");
        assertThat(result.getS3Key()).endsWith("-test.pdf");

        verify(s3Service).uploadFile(
                startsWith("cases/1/"),
                eq(file.getBytes()),
                eq("application/pdf")
        );
        verify(caseFileRepository).save(any(CaseFile.class));
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

        when(caseRecordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseFileService.uploadFile(99L, file))
                .isInstanceOf(CaseRecordNotFoundException.class)
                .hasMessage("Case record not found: 99");

        verify(s3Service, never()).uploadFile(anyString(), any(), anyString());
        verify(caseFileRepository, never()).save(any());
    }

    @Test
    @DisplayName("uploadFile should throw InvalidFileException when filename is blank")
    void uploadFile_shouldThrowInvalidFileException_whenFilenameIsBlank() {
        MultipartFile file = mock(MultipartFile.class);

        when(caseRecordRepository.findById(1L)).thenReturn(Optional.of(caseRecord));
        when(file.getOriginalFilename()).thenReturn(" ");

        assertThatThrownBy(() -> caseFileService.uploadFile(1L, file))
                .isInstanceOf(InvalidFileNameException.class)
                .hasMessage("Filename cannot be blank");

        verify(s3Service, never()).uploadFile(anyString(), any(), anyString());
        verify(caseFileRepository, never()).save(any());
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

        when(caseRecordRepository.findById(1L)).thenReturn(Optional.of(caseRecord));
        when(caseFileRepository.save(any(CaseFile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CaseFile result = caseFileService.uploadFile(1L, file);

        assertThat(result.getContentType()).isEqualTo("application/octet-stream");

        verify(s3Service).uploadFile(
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
        when(caseFileRepository.findByCaseRecordId(1L)).thenReturn(List.of(file1, file2));

        List<CaseFile> result = caseFileService.listFiles(1L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CaseFile::getOriginalFilename)
                .containsExactly("a.pdf", "b.pdf");

        verify(caseRecordRepository).existsById(1L);
        verify(caseFileRepository).findByCaseRecordId(1L);
    }

    @Test
    @DisplayName("listFiles should throw CaseRecordNotFoundException when case record does not exist")
    void listFiles_shouldThrowCaseRecordNotFoundException_whenCaseRecordDoesNotExist() {
        when(caseRecordRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> caseFileService.listFiles(1L))
                .isInstanceOf(CaseRecordNotFoundException.class)
                .hasMessage("Case record not found: 1");

        verify(caseFileRepository, never()).findByCaseRecordId(anyLong());
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
    @DisplayName("downloadFile should return input stream from s3 when file exists")
    void downloadFile_shouldReturnInputStreamFromS3_whenFileExists() throws IOException {
        CaseFile caseFile = new CaseFile();
        caseFile.setId(100L);
        caseFile.setS3Key("cases/1/uuid-test.pdf");

        InputStream inputStream = new ByteArrayInputStream("hello".getBytes());

        when(caseFileRepository.findByIdAndCaseRecordId(100L, 1L))
                .thenReturn(Optional.of(caseFile));
        when(s3Service.downloadFile("cases/1/uuid-test.pdf"))
                .thenReturn(inputStream);

        InputStream result = caseFileService.downloadFile(1L, 100L);

        assertThat(result.readAllBytes()).isEqualTo("hello".getBytes());
        verify(s3Service).downloadFile("cases/1/uuid-test.pdf");
    }

    @Test
    @DisplayName("deleteFile should delete from s3 and repository when file exists")
    void deleteFile_shouldDeleteFromS3AndRepository_whenFileExists() {
        CaseFile caseFile = new CaseFile();
        caseFile.setId(100L);
        caseFile.setS3Key("cases/1/uuid-test.pdf");

        when(caseFileRepository.findByIdAndCaseRecordId(100L, 1L))
                .thenReturn(Optional.of(caseFile));

        caseFileService.deleteFile(1L, 100L);

        verify(s3Service).deleteFile("cases/1/uuid-test.pdf");
        verify(caseFileRepository).delete(caseFile);
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

        when(caseRecordRepository.findById(1L)).thenReturn(Optional.of(caseRecord));
        when(caseFileRepository.save(any(CaseFile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        caseFileService.uploadFile(1L, file);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(s3Service).uploadFile(keyCaptor.capture(), any(byte[].class), eq("application/pdf"));

        String generatedKey = keyCaptor.getValue();
        assertThat(generatedKey).contains("cases/1/");
        assertThat(generatedKey).endsWith("-my_file_1_.pdf");
    }
}
