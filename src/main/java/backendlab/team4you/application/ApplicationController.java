package backendlab.team4you.application;

import backendlab.team4you.booking.BookingEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/application")
    public String application(
            Model model,
            @RequestHeader(value = "HX-Request", required = false) String htmx
    ) {

        model.addAttribute("applications", applicationService.getAll());

        if (htmx != null) {
            return "booking :: content";
        }

        return "application";
    }

    @PostMapping("/application")
    public String createApplication(
            @ModelAttribute ApplicationForm form,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        if (userDetails == null) throw new RuntimeException("User not logged in");

        applicationService.createApplication(form, userDetails.getUsername());


        model.addAttribute("applications", applicationService.getByUsername(userDetails.getUsername()));


        return "application :: content";
    }

    @PostMapping("/application/{id}/extend")
    public String extendApplication(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {


        applicationService.extendApplication(id, userDetails.getUsername());

        return "redirect:/profile";
    }

    @GetMapping("/application/{id}/extend")
    public String extendPage(@PathVariable Long id, Model model) {
        model.addAttribute("applicationId", id);
        return "application-extend";
    }

    @GetMapping("/application/apply")
    public String applyPage(
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest
    ) {
        if (htmxRequest != null) {

            return "application-apply :: content";
        }
        return "application-apply";
    }

    @GetMapping("/application/{id}")
    public String applicationDetail(
            @PathVariable Long id,
            Model model,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest
    ) {
        var app = applicationService.findById(id);
        model.addAttribute("application", app);

        if (htmxRequest != null) {
            return "application-detail :: content";
        }

        return "application-detail";

    }

    @GetMapping("/application/cancelled")
    public String cancelledApplications(
            Model model,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest
    ) {
        List<ApplicationEntity> cancelledApps =
                applicationService.getByUsernameAndStatus(userDetails.getUsername(), "CANCELLED");

        model.addAttribute("applications", cancelledApps);

        if (htmxRequest != null) {
            return "application-cancelled :: content";
        }
        return "application-cancelled";
    }

    @PostMapping("/application/{id}/cancel")
    public String cancelApplication(
            @PathVariable Long id,
            Model model,
            @AuthenticationPrincipal UserDetails user,
            @RequestHeader(value = "HX-Request", required = false) String htmx
    ) {

        ApplicationEntity app = applicationService.findById(id);


        if (user != null && !app.getOwner().getUsername().equals(user.getUsername())) {
            throw new RuntimeException("Not allowed");
        }

        applicationService.cancel(id);

        model.addAttribute("application", app);

        if (htmx != null) {
            return "fragments/application-cancelled :: content";
        }

        return "application-cancelled";
    }



}
