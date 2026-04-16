package backendlab.team4you.casefile;

import backendlab.team4you.caserecord.CaseRecord;
import backendlab.team4you.caserecord.CaseRecordRepository;
import backendlab.team4you.exceptions.CaseFileNotFoundException;
import backendlab.team4you.exceptions.CaseRecordNotFoundException;
import backendlab.team4you.exceptions.InvalidFileNameException;
import backendlab.team4you.s3.S3Service;
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
        CaseRecord caseRecord = caseRecordRepository.findById(caseRecordId)
                .orElseThrow(() -> new CaseRecordNotFoundException(caseRecordId));

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new InvalidFileNameException("Filename cannot be blank");
        }

        String contentType = file.getContentType() != null
                ? file.getContentType()
                : "application/octet-stream";

        String s3Key = buildS3Key(caseRecord, originalFilename);

        try {
            s3Service.uploadFile(s3Key, file.getBytes(), contentType);

            CaseFile caseFile = new CaseFile();
            caseFile.setCaseRecord(caseRecord);
            caseFile.setOriginalFilename(originalFilename);
            caseFile.setS3Key(s3Key);
            caseFile.setContentType(contentType);
            caseFile.setSize(file.getSize());
            caseFile.setUploadedAt(LocalDateTime.now());

            return caseFileRepository.save(caseFile);
        } catch (RuntimeException | IOException e) {
            throw e;
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

    private String buildS3Key(CaseRecord caseRecord, String originalFilename) {
        String safeFilename = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        return "cases/" + caseRecord.getId() + "/" + UUID.randomUUID() + "-" + safeFilename;
    }

    private void ensureCaseRecordExists(Long caseRecordId) {
        if (!caseRecordRepository.existsById(caseRecordId)) {
            throw new CaseRecordNotFoundException(caseRecordId);
        }
    }

}
