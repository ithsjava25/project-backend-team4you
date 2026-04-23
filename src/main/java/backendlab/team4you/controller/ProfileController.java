package backendlab.team4you.controller;


import backendlab.team4you.application.ApplicationService;
import backendlab.team4you.booking.BookingService;
import backendlab.team4you.user.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    private final UserService userService;
    private final ApplicationService applicationService;
    private final BookingService bookingService;

    public ProfileController(UserService userService, ApplicationService applicationService, BookingService bookingService) {
        this.userService = userService;
        this.applicationService = applicationService;
        this.bookingService = bookingService;
    }

    @GetMapping("/profile")
    public String profile(
            Model model,
            @AuthenticationPrincipal UserDetails user
    ) {

        String username = user.getUsername();

        model.addAttribute("applicationCount",
                applicationService.getByUsername(username).size());

        model.addAttribute("bookingCount",
                bookingService.getByUsername(username).size());

        return "profile";
    }

    @DeleteMapping("/profile")
    public String deleteAccount(
            @AuthenticationPrincipal UserDetails user,
            HttpServletResponse response
    ) {

        userService.deleteByUsername(user.getUsername());

        response.setHeader("HX-Redirect", "/");

        return null;
    }

}
