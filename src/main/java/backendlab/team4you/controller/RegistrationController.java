package backendlab.team4you.controller;

import backendlab.team4you.dto.UserRegistrationDTO;
import backendlab.team4you.exceptions.DuplicateEmailException;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserService;
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

    public RegistrationController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {

        model.addAttribute("user", new UserRegistrationDTO("", "", "", "", "", ""));
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") UserRegistrationDTO registrationDto, Model model) {


        if (!registrationDto.password().equals(registrationDto.confirmPassword())) {
            model.addAttribute("error", "Lösenorden matchar inte!");
            return "register";
        }

        try {
            userService.registerUser(registrationDto);
         } catch (DuplicateEmailException e) {
        model.addAttribute("error", "E-postadressen är redan registrerad!");
        return "register";
               } catch (Exception e) {
                   model.addAttribute("error", "Ett oväntat fel uppstod. Försök igen.");
                    return "register";
    }

        return "redirect:/login?registered";
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

        String email = principal.getName();
        UserEntity user = userService.findByEmail(email);

        if (user == null) {

            return "redirect:/login?error=user_not_found";
        }

        model.addAttribute("fullName", user.getFirstName() + " " + user.getLastName());
        return "welcome";


    }
}
