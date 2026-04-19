package backendlab.team4you.controller;

import backendlab.team4you.application.ApplicationForm;
import backendlab.team4you.application.ApplicationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
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
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest
    ) {

        List<String> application = Arrays.asList(
                "Ärende #1 - Pågående",
                "Ärende #15 - Väntar på beslut"
        );

        model.addAttribute("application", application);
        model.addAttribute("userName", "Test user");

        if (htmxRequest != null) {
            return "fragments/application :: content";
        }

        return "application";
    }

    @PostMapping("/application")
    public String createApplication(
            @ModelAttribute ApplicationForm form,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        applicationService.createApplication(form, userDetails.getUsername());

        return "redirect:/profile";
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


}
