package backendlab.team4you.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.jspecify.annotations.Nullable;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_user")
public class UserEntity implements PublicKeyCredentialUserEntity {

    @Id
    @Column(name = "id", length = 255)
    private String id;

    @Column(unique = true, nullable = false)
    private String name;

    private String displayName;

    @Column(name = "password_hash", nullable = true)
    private String passwordHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public UserEntity() {
    }

    public UserEntity(Bytes id, String name, String displayName) {
        this.id = id != null ? id.toBase64UrlString() : null;
        this.name = name;
        this.displayName = displayName;
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public Bytes getId() {
        return id != null ? Bytes.fromBase64(id) : null;
    }

    public void setId(Bytes id) {
        this.id = id != null ? id.toBase64UrlString() : null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public @Nullable String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
