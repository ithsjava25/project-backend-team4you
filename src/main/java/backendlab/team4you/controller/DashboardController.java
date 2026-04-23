package backendlab.team4you.controller;

import backendlab.team4you.application.ApplicationService;
import backendlab.team4you.booking.BookingService;
import backendlab.team4you.user.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@Controller
public class DashboardController {

    private final UserService userService;
    private final ApplicationService applicationService;
    private final BookingService bookingService;


    public DashboardController(UserService userService, ApplicationService applicationService, BookingService bookingService){
        this.userService = userService;
        this.applicationService = applicationService;
        this.bookingService = bookingService;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            Model model,
            @AuthenticationPrincipal UserDetails user,
            @RequestHeader(value = "HX-Request", required = false) String htmx
    ) {

        String username = user.getUsername();

        int activeCount = applicationService.getActiveByUsername(username).size();
        int cancelledCount = applicationService.getCancelledByUsername(username).size();

        model.addAttribute("activeCount",
                applicationService.getActiveByUsername(username).size());

        model.addAttribute("cancelledCount",
                applicationService.getCancelledByUsername(username).size());

        model.addAttribute("bookingCount",
                bookingService.getByUsername(username).size());

        if (htmx != null) {
            return "dashboard :: content";
        }

        return "dashboard";
    }

    @GetMapping("/dashboard/home")
    public String dashboardHome() {
        return "dashboard :: content";
    }
}
