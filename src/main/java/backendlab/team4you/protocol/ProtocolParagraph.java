package backendlab.team4you.protocol;

import backendlab.team4you.caserecord.CaseRecord;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "protocol_paragraph")
public class ProtocolParagraph {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "protocol_id", nullable = false)
    private Protocol protocol;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "case_record_id", nullable = false)
    private CaseRecord caseRecord;

    @Column(name = "paragraph_number", nullable = false)
    private Long paragraphNumber;

    @Column(nullable = false, length = 255)
    private String heading;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_type", length = 30)
    private ProtocolDecisionType decisionType;

    @Column(name = "decision_text", length = 2000)
    private String decisionText;

    protected ProtocolParagraph() {
    }

    public ProtocolParagraph(
            CaseRecord caseRecord,
            Long paragraphNumber,
            String heading
    ) {
        this.caseRecord = Objects.requireNonNull(caseRecord, "caseRecord is required");

        if (paragraphNumber == null || paragraphNumber <= 0) {
            throw new IllegalArgumentException("paragraphNumber must be >= 1");
        }
        this.paragraphNumber = paragraphNumber;

        if (heading == null || heading.isBlank()) {
            throw new IllegalArgumentException("heading is required");
        }
        this.heading = heading.trim();
    }

    public void updateDecision(ProtocolDecisionType decisionType, String decisionText) {
        this.decisionType = Objects.requireNonNull(decisionType, "decisionType is required");

        if (decisionText == null || decisionText.isBlank()) {
            throw new IllegalArgumentException("decisionText is required");
        }

        if (decisionText.length() > 2000) {
            throw new IllegalArgumentException("decisionText length must be <= 2000");
        }

        this.decisionText = decisionText.trim();
    }

    void setProtocol(Protocol protocol) {
        this.protocol = Objects.requireNonNull(protocol, "protocol is required");
    }

    public Long getId() {
        return id;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public CaseRecord getCaseRecord() {
        return caseRecord;
    }

    public Long getParagraphNumber() {
        return paragraphNumber;
    }

    public String getHeading() {
        return heading;
    }

    public ProtocolDecisionType getDecisionType() {
        return decisionType;
    }

    public String getDecisionText() {
        return decisionText;
    }
}
