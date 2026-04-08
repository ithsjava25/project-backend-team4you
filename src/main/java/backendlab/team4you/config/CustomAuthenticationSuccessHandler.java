package backendlab.team4you.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserCredentialRepository userCredentialRepository;
    private final PublicKeyCredentialUserEntityRepository userEntityRepository;

    public CustomAuthenticationSuccessHandler(@Lazy UserCredentialRepository userCredentialRepository,
                                              @Lazy PublicKeyCredentialUserEntityRepository userEntityRepository) {
        this.userCredentialRepository = userCredentialRepository;
        this.userEntityRepository = userEntityRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String username = authentication.getName();

        var userEntity = userEntityRepository.findByEmail(username);

        if (userEntity != null){
            var credentials = userCredentialRepository.findByUserId(userEntity.getId());

            if (!credentials.isEmpty()){
                getRedirectStrategy().sendRedirect(request, response, "/webauthn-check");
                return;
            }
        }
        getRedirectStrategy().sendRedirect(request, response, "/dashboard");
    }
}
