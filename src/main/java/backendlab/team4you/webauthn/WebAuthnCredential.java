package backendlab.team4you.webauthn;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.jspecify.annotations.Nullable;
import org.springframework.security.web.webauthn.api.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "user_credentials")
public class WebAuthnCredential implements CredentialRecord {

    @Id
    @Column(name = "credential_id", length = 512)
    private String credentialId;

    @Column(name = "user_entity_user_id", length = 255)
    private String userEntityUserId;

    @Column(name = "public_key", columnDefinition = "bytea")
    private byte[] publicKey;

    @Column(name = "attestation_object", columnDefinition = "bytea")
    private byte[] attestationObject;

    @Column(name = "attestation_client_data_json", columnDefinition = "bytea")
    private byte[] attestationClientDataJson;

    @Column(name = "signature_count")
    private Long signatureCount;

    @Column(name = "uv_initialized")
    private Boolean uvInitialized;

    @Column(name = "backup_eligible")
    private Boolean backupEligible;

    @Column(name = "backup_state")
    private Boolean backupState;

    @Column(name = "created")
    private Instant created = Instant.now();

    @Column(name = "last_used")
    private Instant lastUsed;

    private String label;

    @Column(name = "public_key_credential_type")
    private String credentialType = "public-key";

    @Column(name = "authenticator_transports")
    private String transports;


    // --- Interface Implementations ---
    public void setCredentialId(Bytes id) {
        this.credentialId = id != null ? id.toBase64UrlString() : null;
    }

    @Override
    public Bytes getCredentialId() {
        return credentialId != null ? Bytes.fromBase64(credentialId) : null;
    }

    @Override
    public Bytes getUserEntityUserId() {
        return userEntityUserId != null ? Bytes.fromBase64(userEntityUserId) : null;
    }

    @Override
    public PublicKeyCose getPublicKey() {
        return publicKey != null ? new ImmutablePublicKeyCose(publicKey) : null;
    }

    public void setPublicKey(PublicKeyCose key) {
        this.publicKey = key != null ? key.getBytes() : null;
    }

    @Override
    public Set<AuthenticatorTransport> getTransports() {
        if (transports == null || transports.isEmpty()) return Collections.emptySet();
        return Arrays.stream(transports.split(","))
                .map(AuthenticatorTransport::valueOf)
                .collect(Collectors.toSet());
    }

    public void setTransports(Set<AuthenticatorTransport> transports) {
        this.transports = transports != null ?
                transports.stream().map(AuthenticatorTransport::getValue).collect(Collectors.joining(",")) : null;
    }

    @Override
    public PublicKeyCredentialType getCredentialType() {
        return PublicKeyCredentialType.PUBLIC_KEY;
    }

    // Standard Getters for remaining interface methods
    @Override
    public long getSignatureCount() {
        return signatureCount;
    }

    public void setSignatureCount(long count) {
        this.signatureCount = count;
    }

    @Override
    public boolean isUvInitialized() {
        return uvInitialized;
    }

    public void setUvInitialized(boolean uv) {
        this.uvInitialized = uv;
    }

    @Override
    public boolean isBackupEligible() {
        return backupEligible;
    }

    public void setBackupEligible(boolean be) {
        this.backupEligible = be;
    }

    @Override
    public boolean isBackupState() {
        return backupState;
    }

    public void setBackupState(boolean bs) {
        this.backupState = bs;
    }

    @Override
    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    @Override
    public Instant getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(Instant lastUsed) {
        this.lastUsed = lastUsed;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public @Nullable Bytes getAttestationObject() {
        return attestationObject != null ? new Bytes(attestationObject) : null;
    }

    public void setAttestationObject(byte[] attestationObject) {
        this.attestationObject = attestationObject;
    }

    @Override
    public @Nullable Bytes getAttestationClientDataJSON() {
        return attestationClientDataJson != null ? new Bytes(attestationClientDataJson) : null;
    }

    public void setAttestationClientDataJSON(byte[] attestationClientDataJson) {
        this.attestationClientDataJson = attestationClientDataJson;
    }
}
