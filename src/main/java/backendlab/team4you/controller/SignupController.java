package backendlab.team4you.controller;

import backendlab.team4you.audit.AuditAction;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class SignupController {

    private final PublicKeyCredentialUserEntityRepository users;
    private final UserService userService;

    public SignupController(PublicKeyCredentialUserEntityRepository users,
                            UserService userService) {
        this.users = users;
        this.userService = userService;
    }

    @GetMapping("/login/webauthn")
    public String webauthnCheck() {
        return "check";
    }

    @GetMapping("/signup")
    String signup(org.springframework.security.web.csrf.CsrfToken token, Model model) {
        model.addAttribute("csrfToken", token.getToken());
        return "signup";
    }

    @PostMapping("/signup")
    @ResponseBody
    @AuditAction(action = "SIGNUP", entity = "USER")
    public void signup(@RequestBody SignupRequest req, HttpServletRequest request, HttpServletResponse response) {

        UserEntity userEntity = userService.registerWebAuthnUser(
                req.getUsername(),
                req.getDisplayName(),
                req.getEmail(),
                req.getFirstName(),
                req.getLastName()
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(
                userEntity.getName(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + userEntity.getRole().name()))
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);

        SecurityContextRepository repo = new HttpSessionSecurityContextRepository();
        repo.saveContext(context, request, response);
    }

    public static class SignupRequest {
        private String username;
        private String displayName;
        private String email;
        private String firstName;
        private String lastName;

        public SignupRequest() {
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }


}
