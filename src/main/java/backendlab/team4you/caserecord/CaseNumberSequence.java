package backendlab.team4you.caserecord;

import backendlab.team4you.registry.Registry;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

@Entity
@Table(
        name = "case_number_sequence",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_case_number_sequence_registry_year",
                        columnNames = {"registry_id", "sequence_year"}
                )
        }
)
public class CaseNumberSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "registry_id", nullable = false)
    private Registry registry;

    @Column(name = "sequence_year",nullable = false)
    private Integer year;

    @Column(name = "last_value", nullable = false)
    @NotNull
    @Min(0)
    private Long lastValue;

    protected CaseNumberSequence() {}

    public CaseNumberSequence(Registry registry, Integer year, Long lastValue) {
        this.registry = Objects.requireNonNull(registry, "registry is required");
        this.year = Objects.requireNonNull(year, "year is required");
        setLastValue(lastValue);
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
