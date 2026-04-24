package backendlab.team4you.controller;

import backendlab.team4you.caserecord.CaseRecord;
import backendlab.team4you.caserecord.CaseRecordRepository;
import backendlab.team4you.caserecord.CaseRecordService;
import backendlab.team4you.caserecord.CaseStatus;
import backendlab.team4you.exceptions.CaseRecordNotFoundException;
import backendlab.team4you.exceptions.UserNotFoundException;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

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
    public String caseOfficerHome() {
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

    @PostMapping("/case-officer/cases/close")
    @ResponseBody
    public String closeCase(@RequestParam String id, Authentication auth) {
        UserEntity officer = userRepository.findByName(auth.getName())
                .orElseThrow(() -> new UserNotFoundException("Case Officer not found"));

        CaseRecord caseRecord = caseRecordRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new CaseRecordNotFoundException(id));

        if (!caseRecord.getAssignedUser().getId().equals(officer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not assigned to this officer");
        }

        caseRecord.setStatus(CaseStatus.CLOSED);
        caseRecordRepository.save(caseRecord);

        return "";
    }
}
