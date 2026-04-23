package backendlab.team4you.controller;

import backendlab.team4you.application.ApplicationEntity;
import backendlab.team4you.application.ApplicationRepository;
import backendlab.team4you.application.ApplicationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@PreAuthorize("hasRole('CASE_OFFICER')")
public class CaseOfficerController {

    private final ApplicationService applicationService;
    private final ApplicationRepository applicationRepository;

    public CaseOfficerController(ApplicationService applicationService, ApplicationRepository applicationRepository) {
        this.applicationService = applicationService;
        this.applicationRepository = applicationRepository;
    }

    @GetMapping("/case-officer")
    public String admin(Authentication auth) {
        System.out.println(auth.getAuthorities());
        return "case-officer";
    }

    @GetMapping("/case-officer/applications")
    public String listApplications(
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {

        Page<ApplicationEntity> applications =
                applicationRepository.findAll(PageRequest.of(page, 5));

        model.addAttribute("applications", applications.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", applications.getTotalPages());

        return "fragments/case-officer-applications :: content";
    }

    @PostMapping("/case-officer/applications/delete")
    public String deleteApplication(@RequestParam Long id, Model model) {

        applicationService.delete(id);

        model.addAttribute("message", "Ansökan borttagen");

        return "fragments/alert :: success";
    }


}
