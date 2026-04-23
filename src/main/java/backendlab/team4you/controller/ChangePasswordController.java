package backendlab.team4you.controller;

import backendlab.team4you.user.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ChangePasswordController {

    private final UserService userService;

    public ChangePasswordController(UserService userService) {
        this.userService = userService;
    }




    @PostMapping("/profile/change-password")
    public String changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @AuthenticationPrincipal UserDetails user,
            Model model
    ) {


        boolean success = userService.changePassword(
                user.getUsername(),
                oldPassword,
                newPassword
        );

        model.addAttribute("success", success);

        return "fragments/change-password :: content";

    }

    @GetMapping("/profile/change-password")
    public String changePasswordPage() {
        return "fragments/change-password :: content";
    }
}
