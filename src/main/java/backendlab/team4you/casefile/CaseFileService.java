package backendlab.team4you.casefile;

import backendlab.team4you.caserecord.CaseRecord;
import backendlab.team4you.caserecord.CaseRecordRepository;
import backendlab.team4you.exceptions.CaseFileNotFoundException;
import backendlab.team4you.exceptions.CaseRecordNotFoundException;
import backendlab.team4you.exceptions.InvalidFileNameException;
import backendlab.team4you.s3.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CaseFileService {

    private static final Logger log = LoggerFactory.getLogger(CaseFileService.class);
    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;

    private final CaseRecordRepository caseRecordRepository;
    private final CaseFileRepository caseFileRepository;
    private final S3Service s3Service;

    public CaseFileService(
            CaseRecordRepository caseRecordRepository,
            CaseFileRepository caseFileRepository,
            S3Service s3Service
    )  {
        this.caseRecordRepository = caseRecordRepository;
        this.caseFileRepository = caseFileRepository;
        this.s3Service = s3Service;
    }

    @Transactional
    public CaseFile uploadFile(Long caseRecordId, MultipartFile file) throws IOException {

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File exceeds maximum size");
        }

        CaseRecord caseRecord = caseRecordRepository.findById(caseRecordId)
                .orElseThrow(() -> new CaseRecordNotFoundException(caseRecordId));

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new InvalidFileNameException("Filename must not be blank");
        }

        String contentType = normalizeContentType(file.getContentType());
        String s3Key = buildUniqueS3Key(caseRecord, originalFilename);

        boolean uploadedToS3 = false;

        try {
            s3Service.uploadFileIfAbsent(s3Key, file.getBytes(), contentType);
            uploadedToS3 = true;

            CaseFile caseFile = new CaseFile();
            caseFile.setCaseRecord(caseRecord);
            caseFile.setOriginalFilename(originalFilename);
            caseFile.setS3Key(s3Key);
            caseFile.setContentType(contentType);
            caseFile.setSize(file.getSize());
            caseFile.setUploadedAt(LocalDateTime.now());

            return caseFileRepository.saveAndFlush(caseFile);

        } catch (RuntimeException | IOException exception) {
            if (uploadedToS3) {
                cleanupUploadedObjectIfPossible(s3Key, exception);
            }
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<CaseFile> listFiles(Long caseRecordId) {
        ensureCaseRecordExists(caseRecordId);
        return caseFileRepository.findByCaseRecordId(caseRecordId);
    }

    @Transactional(readOnly = true)
    public CaseFile getCaseFile(Long caseRecordId, Long fileId) {
        return caseFileRepository.findByIdAndCaseRecordId(fileId, caseRecordId)
                .orElseThrow(() -> new CaseFileNotFoundException(caseRecordId, fileId));
    }

    @Transactional(readOnly = true)
    public InputStream downloadFile(Long caseRecordId, Long fileId) {
        CaseFile caseFile = getCaseFile(caseRecordId, fileId);
        return s3Service.downloadFile(caseFile.getS3Key());
    }

    @Transactional
    public void deleteFile(Long caseRecordId, Long fileId) {
        CaseFile caseFile = getCaseFile(caseRecordId, fileId);

        s3Service.deleteFile(caseFile.getS3Key());
        caseFileRepository.delete(caseFile);
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "application/octet-stream";
        }
        return contentType;
    }

    private String buildUniqueS3Key(CaseRecord caseRecord, String originalFilename) {
        String safeFilename = sanitizeFilename(originalFilename);
        return "cases/" + caseRecord.getId() + "/" + UUID.randomUUID() + "-" + safeFilename;
    }

    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private void ensureCaseRecordExists(Long caseRecordId) {
        if (!caseRecordRepository.existsById(caseRecordId)) {
            throw new CaseRecordNotFoundException(caseRecordId);
        }
    }

    private void cleanupUploadedObjectIfPossible(String s3Key, Exception originalException) {
        try {
            s3Service.deleteFile(s3Key);
        } catch (Exception cleanupException) {
            log.error(
                    "Failed to clean up S3 object after upload/save failure. key={}",
                    s3Key,
                    cleanupException
            );
        }

        if (originalException instanceof DataIntegrityViolationException) {
            log.warn("Database integrity violation after S3 upload. Attempted cleanup for key={}", s3Key);
        }
    }
}
