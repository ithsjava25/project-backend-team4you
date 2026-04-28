package backendlab.team4you.audit;


import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationEvents {

    private final AuditService auditService;

    public AuthenticationEvents(AuditService auditService) {
        this.auditService = auditService;
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        Authentication auth = event.getAuthentication();
        String username = auth.getName();
        String methodDetails = "Inloggning via standardmetod";

        auditService.saveLog(
                username, null, "LOGIN", "/login", "POST",
                "N/A", "SUCCESS", "USER", 0
        );
        if (auth.getClass().getSimpleName().contains("WebAuthn")) {
            methodDetails = "Inloggning med Passkey (WebAuthn)";
        }

        auditService.saveLog(
                username, null, "LOGIN", "/login", "POST",
                "N/A", "SUCCESS", "USER", 0
        );
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();

        auditService.saveLog(
                username, null, "LOGIN_FAILED", "/login", "POST",
                "N/A", "FAILED", "USER", 0
        );
    }


}
