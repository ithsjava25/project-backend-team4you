package backendlab.team4you.webauthn;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.jspecify.annotations.Nullable;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;

@Entity
@Table(name = "user_entities")
public class UserEntity implements PublicKeyCredentialUserEntity {

    @Id
    @Column(name = "id", length = 255)
    private String id;

    private String name;

    private String displayName;

    public UserEntity() {
    }

    public UserEntity(Bytes id, String name, String displayName) {
        this.id = id != null ? id.toBase64UrlString() : null;
        this.name = name;
        this.displayName = displayName;
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
}
