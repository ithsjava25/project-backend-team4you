package backendlab.team4you.casefile;

import backendlab.team4you.caserecord.CaseRecord;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class CaseFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "case_record_id", nullable = false)
    private CaseRecord caseRecord;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String s3Key;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private long size;

    @Column(nullable = false)
    private String bucketName;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;
}
