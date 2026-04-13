package backendlab.team4you.registry;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

@Entity
@Table(name= "registry")
public class Registry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 2)
    @Pattern(regexp = "[A-Z]{2}", message = "code must be exactly 2 uppercase letters")
    private String code;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    protected Registry() {}

    public Registry(String name, String code) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        this.name = name.trim();
        if (code == null) {
            throw new IllegalArgumentException("code must be exactly 2 uppercase letters");
        }
        String normalizedCode = code.trim();
        if (!normalizedCode.matches("[A-Z]{2}")) {
            throw new IllegalArgumentException("code must be exactly 2 uppercase letters");
        }
        this.code = normalizedCode;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }
}
