package backendlab.team4you.casefile;

import backendlab.team4you.casefile.access.CaseFileAccessService;
import backendlab.team4you.caserecord.CaseRecord;
import backendlab.team4you.caserecord.CaseRecordRepository;
import backendlab.team4you.common.ConfidentialityLevel;
import backendlab.team4you.exceptions.*;
import backendlab.team4you.meeting.MeetingAgendaDocumentRepository;
import backendlab.team4you.s3.S3Service;
import backendlab.team4you.user.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

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
    private final CaseFileAccessService caseFileAccessService;
    private final S3Service s3Service;
    private final MeetingAgendaDocumentRepository meetingAgendaDocumentRepository;

    public CaseFileService(
            CaseRecordRepository caseRecordRepository,
            CaseFileRepository caseFileRepository,
            CaseFileAccessService caseFileAccessService,
            S3Service s3Service,
            MeetingAgendaDocumentRepository meetingAgendaDocumentRepository
    ) {
        this.caseRecordRepository = caseRecordRepository;
        this.caseFileRepository = caseFileRepository;
        this.caseFileAccessService = caseFileAccessService;
        this.s3Service = s3Service;
        this.meetingAgendaDocumentRepository = meetingAgendaDocumentRepository;
    }

    @Transactional
    public CaseFile uploadFile(
            Long caseRecordId,
            MultipartFile file,
            ConfidentialityLevel confidentialityLevel,
            UserEntity actor
    ) throws IOException {

        ConfidentialityLevel effectiveConfidentialityLevel =
                confidentialityLevel != null ? confidentialityLevel : ConfidentialityLevel.OPEN;

        CaseRecord caseRecord = caseRecordRepository.findByIdWithLock(caseRecordId)
                .orElseThrow(() -> new CaseRecordNotFoundException(caseRecordId));

        if (!caseFileAccessService.canUploadFile(actor, caseRecord, effectiveConfidentialityLevel)) {
            throw new AccessDeniedException("Du har inte behörighet att ladda upp denna fil.");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new FileTooLargeException(MAX_FILE_SIZE_BYTES);
        }

        int nextDocumentNumber = allocateNextDocumentNumber(caseRecord.getId());
        String documentReference = caseRecord.getCaseNumber() + "-" + nextDocumentNumber;

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new InvalidFileNameException("Filnamn måste anges.");
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
            caseFile.setDocumentNumber(nextDocumentNumber);
            caseFile.setDocumentReference(documentReference);
            caseFile.setConfidentialityLevel(effectiveConfidentialityLevel);

            return caseFileRepository.saveAndFlush(caseFile);

        } catch (NoSuchBucketException exception) {
            throw new FileStorageConfigurationException(
                    "Filuppladdning är inte korrekt konfigurerad: S3-bucket saknas.",
                    exception
            );
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
        return caseFileRepository.findByCaseRecordIdOrderByDocumentNumberAsc(caseRecordId);
    }

    @Transactional(readOnly = true)
    public List<CaseFileListItemDto> listFileItemsForViewer(Long caseRecordId, UserEntity viewer) {
        ensureCaseRecordExists(caseRecordId);

        return caseFileRepository.findByCaseRecordIdOrderByDocumentNumberAsc(caseRecordId).stream()
                .map(file -> {
                    boolean canView = caseFileAccessService.canViewFile(viewer, file);
                    boolean confidential = file.getConfidentialityLevel() == ConfidentialityLevel.CONFIDENTIAL;

                    String displayName = (!confidential || canView)
                            ? file.getOriginalFilename()
                            : "Sekretess";

                    boolean canDownload = !confidential || canView;

                    return new CaseFileListItemDto(
                            file.getId(),
                            file.getDocumentReference(),
                            displayName,
                            confidential,
                            canDownload
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public CaseFile getCaseFile(Long caseRecordId, Long fileId) {
        return caseFileRepository.findByIdAndCaseRecordId(fileId, caseRecordId)
                .orElseThrow(() -> new CaseFileNotFoundException(caseRecordId, fileId));
    }

    @Transactional(readOnly = true)
    public CaseFile getCaseFileForViewer(Long caseRecordId, Long fileId, UserEntity viewer) {
        CaseFile caseFile = getCaseFile(caseRecordId, fileId);

        if (!caseFileAccessService.canViewFile(viewer, caseFile)) {
            throw new AccessDeniedException("Du har inte behörighet att öppna denna fil.");
        }

        return caseFile;
    }

    @Transactional(readOnly = true)
    public InputStream downloadFile(Long caseRecordId, Long fileId, UserEntity viewer) {
        CaseFile caseFile = getCaseFileForViewer(caseRecordId, fileId, viewer);
        return s3Service.downloadFile(caseFile.getS3Key());
    }

    @Transactional
    public void deleteFile(Long caseRecordId, Long fileId, UserEntity actor) {
        CaseFile caseFile = getCaseFile(caseRecordId, fileId);

        if (!caseFileAccessService.canDeleteFile(actor, caseFile)) {
            throw new AccessDeniedException("Du har inte behörighet att radera denna fil.");
        }

        String s3Key = caseFile.getS3Key();

        if (meetingAgendaDocumentRepository.existsByCaseFileId(fileId)) {
            throw new FileInUseException("Filen kan inte tas bort eftersom den används som mötesunderlag.");
        }

        caseFileRepository.delete(caseFile);

        try {
            s3Service.deleteFile(s3Key);
        } catch (Exception exception) {
            log.error("Failed to delete S3 object after DB deletion: {}", s3Key, exception);
        }
    }

    @Transactional
    public CaseFile uploadGeneratedFile(
            Long caseRecordId,
            String originalFilename,
            String contentType,
            byte[] bytes,
            ConfidentialityLevel confidentialityLevel,
            UserEntity actor
    ) {
        ConfidentialityLevel effectiveConfidentialityLevel =
                confidentialityLevel != null ? confidentialityLevel : ConfidentialityLevel.OPEN;

        CaseRecord caseRecord = caseRecordRepository.findByIdWithLock(caseRecordId)
                .orElseThrow(() -> new CaseRecordNotFoundException(caseRecordId));

        if (!caseFileAccessService.canUploadFile(actor, caseRecord, effectiveConfidentialityLevel)) {
            throw new AccessDeniedException("Du har inte behörighet att ladda upp denna fil.");
        }

        if (bytes.length > MAX_FILE_SIZE_BYTES) {
            throw new FileTooLargeException(MAX_FILE_SIZE_BYTES);
        }

        if (originalFilename == null || originalFilename.isBlank()) {
            throw new InvalidFileNameException("Filnamn måste anges.");
        }

        String normalizedContentType = normalizeContentType(contentType);

        int nextDocumentNumber = allocateNextDocumentNumber(caseRecord.getId());
        String documentReference = caseRecord.getCaseNumber() + "-" + nextDocumentNumber;
        String s3Key = buildUniqueS3Key(caseRecord, originalFilename);

        boolean uploadedToS3 = false;

        try {
            s3Service.uploadFileIfAbsent(s3Key, bytes, normalizedContentType);
            uploadedToS3 = true;

            CaseFile caseFile = new CaseFile();
            caseFile.setCaseRecord(caseRecord);
            caseFile.setOriginalFilename(originalFilename);
            caseFile.setS3Key(s3Key);
            caseFile.setContentType(normalizedContentType);
            caseFile.setSize(bytes.length);
            caseFile.setUploadedAt(LocalDateTime.now());
            caseFile.setDocumentNumber(nextDocumentNumber);
            caseFile.setDocumentReference(documentReference);
            caseFile.setConfidentialityLevel(effectiveConfidentialityLevel);

            return caseFileRepository.saveAndFlush(caseFile);

        } catch (NoSuchBucketException exception) {
            throw new FileStorageConfigurationException(
                    "Filuppladdning är inte korrekt konfigurerad: S3-bucket saknas.",
                    exception
            );
        } catch (RuntimeException exception) {
            if (uploadedToS3) {
                cleanupUploadedObjectIfPossible(s3Key, exception);
            }
            throw exception;
        }
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        try {
            return MediaType.parseMediaType(contentType).toString();
        } catch (InvalidMediaTypeException exception) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
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
            log.warn(
                    "Data integrity violation while persisting CaseFile (possible duplicate s3_key or invalid FK/NULL). Attempted cleanup for key={}",
                    s3Key
            );
        }
    }

    private int allocateNextDocumentNumber(Long caseRecordId) {
        return caseFileRepository.findTopByCaseRecordIdOrderByDocumentNumberDesc(caseRecordId)
                .map(caseFile -> caseFile.getDocumentNumber() + 1)
                .orElse(1);
    }
}
