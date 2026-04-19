package backendlab.team4you.user;

import backendlab.team4you.webauthn.WebAuthnCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCredentialRepository extends JpaRepository<WebAuthnCredential, String> {

    List<WebAuthnCredential> findByUserEntityUserId(String userEntityUserId);

}
