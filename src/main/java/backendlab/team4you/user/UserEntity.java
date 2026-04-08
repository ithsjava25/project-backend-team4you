package backendlab.team4you.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_entities")
public class UserEntity implements PublicKeyCredentialUserEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name", nullable = false, unique = true)
    private String email;

    @Column(name = "display_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "role")
    private String role = "USER";

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public UserEntity() {
    }

    public UserEntity(Bytes id, String email, String firstName) {
        this.id = id != null ? id.toBase64UrlString() : null;
        this.email = email;
        this.firstName = firstName;
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
        return this.email;
    }

    @Override
    public String getDisplayName() {
        return (this.firstName != null ? this.firstName : "") + " " + (this.lastName != null ? this.lastName : "");
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

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
