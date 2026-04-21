package backendlab.team4you.casefile;

import backendlab.team4you.caserecord.CaseRecord;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "case_file",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_case_file_s3_key", columnNames = "s3_key"),
                @UniqueConstraint(name = "uk_case_file_case_record_document_number",
                        columnNames = {"case_record_id", "document_number"}),
                @UniqueConstraint(name = "uk_case_file_document_reference",
                        columnNames = "document_reference")
        }
)
public class CaseFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "case_record_id", nullable = false)
    private CaseRecord caseRecord;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "s3_key", nullable = false, length = 1024)
    private String s3Key;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "size_in_bytes", nullable = false)
    private long size;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "document_number", nullable = false)
    private int documentNumber;

    @Column(name = "document_reference", nullable = false)
    private String documentReference;

    public String getS3Key() {
        return s3Key;
    }

    public void setCaseRecord(CaseRecord caseRecord) {
        this.caseRecord = caseRecord;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFileName = originalFilename;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setUploadedAt(LocalDateTime now) {
        this.uploadedAt = now;
    }

    public Long getId() {
        return id;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setId(long l) {
        this.id = l;
    }

    public CaseRecord getCaseRecord() {
        return caseRecord;
    }

    public String getOriginalFilename() {
        return originalFileName;
    }

    public int getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(int documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getDocumentReference() {
        return documentReference;
    }

    public void setDocumentReference(String documentReference) {
        this.documentReference = documentReference;
    }
}
