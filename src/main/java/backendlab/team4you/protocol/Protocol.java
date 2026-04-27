package backendlab.team4you.protocol;

import backendlab.team4you.meeting.Meeting;
import backendlab.team4you.registry.Registry;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        name = "protocol",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_protocol_meeting", columnNames = "meeting_id")
        }
)
public class Protocol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "registry_id", nullable = false)
    private Registry registry;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "protocol_year", nullable = false)
    private Integer year;

    @OneToMany(mappedBy = "protocol", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("paragraphNumber ASC")
    private List<ProtocolParagraph> paragraphs = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Protocol() {
    }

    public Protocol(Meeting meeting, Registry registry, String title, Integer year) {
        this.meeting = Objects.requireNonNull(meeting, "meeting is required");
        this.registry = Objects.requireNonNull(registry, "registry is required");

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        this.title = title.trim();

        if (year == null) {
            throw new IllegalArgumentException("year is required");
        }
        this.year = year;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void addParagraph(ProtocolParagraph paragraph) {
        Objects.requireNonNull(paragraph, "paragraph is required");
        paragraphs.add(paragraph);
        paragraph.setProtocol(this);
    }

    public Long getId() {
        return id;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    public Registry getRegistry() {
        return registry;
    }

    public String getTitle() {
        return title;
    }

    public Integer getYear() {
        return year;
    }

    public List<ProtocolParagraph> getParagraphs() {
        return paragraphs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
