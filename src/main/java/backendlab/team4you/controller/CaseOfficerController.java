package backendlab.team4you.controller;

import backendlab.team4you.application.ApplicationService;
import backendlab.team4you.caserecord.CaseRecord;
import backendlab.team4you.caserecord.CaseRecordRepository;
import backendlab.team4you.exceptions.UserNotFoundException;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserRepository;
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

    private final CaseRecordRepository caseRecordRepository;
    private final UserRepository userRepository;

    public CaseOfficerController(CaseRecordRepository caseRecordRepository, UserRepository userRepository) {
        this.caseRecordRepository = caseRecordRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/case-officer")
    public String admin(Authentication auth) {
        System.out.println(auth.getAuthorities());
        return "case-officer";
    }

    @GetMapping("/case-officer/cases")
    public String listCases(
            @RequestParam(defaultValue = "0") int page,
            Authentication auth,
            Model model
    ) {
        UserEntity officer = userRepository.findByName(auth.getName())
                .orElseThrow(() -> new UserNotFoundException("Case Officer not found"));

        Page<CaseRecord> cases =
                caseRecordRepository.findByAssignedUserId(officer.getIdAsString(), PageRequest.of(page, 5));

        model.addAttribute("cases", cases.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", cases.getTotalPages());

        return "fragments/case-officer-cases :: content";
    }

    //todo: endpoint to delete a case

//    @PostMapping("/case-officer/cases/delete")
//    public String deleteApplication(@RequestParam Long id, Model model) {
//
//        applicationService.delete(id);
//
//        model.addAttribute("message", "Ansökan borttagen");
//
//        return "fragments/alert :: success";
//    }
}
