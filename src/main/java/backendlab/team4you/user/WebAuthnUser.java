package backendlab.team4you.user;

import com.webauthn4j.data.PublicKeyCredentialUserEntity;

import java.nio.charset.StandardCharsets;




public class WebAuthnUser extends PublicKeyCredentialUserEntity {



    public WebAuthnUser(UserEntity user) {
        super(
                user.getId().toString().getBytes(StandardCharsets.UTF_8),
                user.getUsername(),
                user.getDisplayName()
        );
    }
}