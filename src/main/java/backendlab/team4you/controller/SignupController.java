package backendlab.team4you.controller;

import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserService;

import backendlab.team4you.user.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/webauthn-check")
    public String showWebAuthnCheck(){
        return "webauthn-check";
    }

    @GetMapping("/dashboard")
    public String dashboardHome(
            @AuthenticationPrincipal UserDetails user,
            @RequestHeader(value = "HX-Request", required = false) String htmx
    ) {

        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (htmx != null) {
            return isAdmin
                    ? "admin/dashboard :: content"
                    : "user/dashboard :: content";
        }

        return "dashboard";
    }

    @GetMapping("/signup")
    String signup(org.springframework.security.web.csrf.CsrfToken token, Model model) {
        model.addAttribute("csrfToken", token.getToken());
        return "signup";
    }

    @PostMapping("/signup")
    @ResponseBody
    public void signup(@RequestBody SignupRequest req, HttpServletRequest request, HttpServletResponse response) {

        UserEntity userEntity = userService.registerWebAuthnUser(
                req.getUsername(),
                req.getDisplayName(),
                req.getEmail(),
                req.getFirstName(),
                req.getLastName()
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(
                userEntity.getUsername(), null, List.of(new SimpleGrantedAuthority("ROLE_" + userEntity.getRole())));

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
