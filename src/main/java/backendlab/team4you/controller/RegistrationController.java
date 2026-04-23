package backendlab.team4you.controller;

import backendlab.team4you.dto.UserRegistrationDTO;
import backendlab.team4you.exceptions.DuplicateEmailException;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class RegistrationController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public RegistrationController(UserService userService, AuthenticationManager authenticationManager){
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {

        model.addAttribute("user", new UserRegistrationDTO("","", "", "", "", "", "", ""));
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @ModelAttribute("user") UserRegistrationDTO registrationDto,
            Model model
    ) {

        if (!registrationDto.password().equals(registrationDto.confirmPassword())) {
            model.addAttribute("error", "Lösenorden matchar inte!");
            return "register";
        }

        try {
            userService.registerUser(registrationDto);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            registrationDto.email(),
                            registrationDto.password()
                    );

            Authentication authentication =
                    authenticationManager.authenticate(authToken);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            return "redirect:/dashboard";

        } catch (DuplicateEmailException e) {
            model.addAttribute("error", "E-postadressen är redan registrerad!");
            return "register";
        }
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "registered", required = false) String registered,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (registered != null) {
            model.addAttribute("message", "Registreringen lyckades! Logga in här.");
        }
        if (logout != null) {
            model.addAttribute("message", "Du har loggat ut.");
        }
        return "login";
    }

    @GetMapping("/welcome")
    public String welcome(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        String name = principal.getName();
        UserEntity user = userService.findByName(name);

        if (user == null) {

            return "redirect:/login?error=user_not_found";
        }

        model.addAttribute("fullName", user.getFirstName() + " " + user.getLastName());
        return "welcome";


    }
}
