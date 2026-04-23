package backendlab.team4you.config;

import backendlab.team4you.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.webauthn.authentication.WebAuthnAuthenticationRequestToken;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
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
            var credentials = userCredentialRepository.findByUserId(userEntity.getId());

            if (!credentials.isEmpty()){
                getRedirectStrategy().sendRedirect(request, response, "/login/webauthn");
                return;
            }
        }

        var authorities = authentication.getAuthorities();

        boolean isAdmin = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin)
            getRedirectStrategy().sendRedirect(request, response, "/admin");
        else {
            boolean isCaseOfficer = authorities.stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_CASE_OFFICER"));

            if(isCaseOfficer)
                getRedirectStrategy().sendRedirect(request, response, "/case-officer");
            else {
                getRedirectStrategy().sendRedirect(request, response, "/home");
            }
        }
    }
}
