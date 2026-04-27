package backendlab.team4you.protocol;

import backendlab.team4you.registry.Registry;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

@Entity
@Table(
        name = "protocol_paragraph_sequence",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_protocol_paragraph_sequence_registry_year",
                        columnNames = {"registry_id", "sequence_year"}
                )
        }
)
public class ProtocolParagraphSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "registry_id", nullable = false)
    private Registry registry;

    @Column(name = "sequence_year", nullable = false)
    private Integer year;

    @Column(name = "last_value", nullable = false)
    @NotNull
    @Min(0)
    private Long lastValue;

    protected ProtocolParagraphSequence() {
    }

    public ProtocolParagraphSequence(Registry registry, Integer year, Long lastValue) {
        this.registry = Objects.requireNonNull(registry, "registry is required");
        this.year = Objects.requireNonNull(year, "year is required");
        setLastValue(lastValue);
    }

    public void increment() {
        this.lastValue++;
    }

    public Long getId() {
        return id;
    }

    public Registry getRegistry() {
        return registry;
    }

    public Integer getYear() {
        return year;
    }

    public Long getLastValue() {
        return lastValue;
    }

    public void setLastValue(Long lastValue) {
        if (lastValue == null) {
            throw new IllegalArgumentException("lastValue is required");
        }
        if (lastValue < 0) {
            throw new IllegalArgumentException("lastValue must be >= 0");
        }
        this.lastValue = lastValue;
    }
}
