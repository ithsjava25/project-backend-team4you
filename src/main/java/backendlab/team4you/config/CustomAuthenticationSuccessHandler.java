package backendlab.team4you.config;

import backendlab.team4you.user.UserService;
import backendlab.team4you.user.UserCredentialRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserCredentialRepository userCredentialRepository;
    private final UserService userService;

    public CustomAuthenticationSuccessHandler(@Lazy UserCredentialRepository userCredentialRepository,
                                              @Lazy UserService userService){
        this.userCredentialRepository = userCredentialRepository;
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication
    ) throws IOException {

        String username = authentication.getName();

        var userEntity = userService.findByName(username);

        if (userEntity != null){
            var credentials = userCredentialRepository
                    .findByUserEntityUserId(userEntity.getId().toString());

            if (!credentials.isEmpty()){
                getRedirectStrategy().sendRedirect(request, response, "/webauthn-check");
                return;
            }
        }
        getRedirectStrategy().sendRedirect(request, response, "/dashboard");
    }
}