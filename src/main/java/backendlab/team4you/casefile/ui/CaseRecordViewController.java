package backendlab.team4you.casefile.ui;

import backendlab.team4you.caserecord.CaseRecordRequestDto;
import backendlab.team4you.caserecord.CaseRecordService;
import backendlab.team4you.exceptions.CaseRecordNotFoundException;
import backendlab.team4you.exceptions.RegistryNotFoundException;
import backendlab.team4you.exceptions.UserNotFoundException;
import backendlab.team4you.registry.RegistryService;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/dashboard/case-management")
public class CaseRecordViewController {

    private final RegistryService registryService;
    private final CaseRecordService caseRecordService;
    private final UserService userService;

    public CaseRecordViewController(
            RegistryService registryService,
            CaseRecordService caseRecordService,
            UserService userService
    ) {
        this.registryService = registryService;
        this.caseRecordService = caseRecordService;
        this.userService = userService;
    }

    @GetMapping("/registries/{registryId}/case-records")
    public String caseRecords(@PathVariable Long registryId, Model model, Principal principal) {
        UserEntity currentUser = userService.getCurrentUser(principal);
        populateCaseRecordPanelModel(registryId, model, currentUser);
        return "fragments/case-management/case-record-list :: caseRecordList";
    }

    @PostMapping("/registries/{registryId}/case-records")
    public String createCaseRecord(
            @PathVariable Long registryId,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam String status,
            @RequestParam(required = false) String assignedUserId,
            @RequestParam String confidentialityLevel,
            Model model,
            Principal principal
    ) {
        UserEntity currentUser = userService.getCurrentUser(principal);

        try {
            CaseRecordRequestDto requestDto = new CaseRecordRequestDto(
                    registryId,
                    title,
                    description,
                    status,
                    currentUser.getId().toBase64UrlString(),
                    normalizeAssignedUserId(assignedUserId),
                    confidentialityLevel,
                    null
            );

            caseRecordService.createCaseRecord(requestDto);
            model.addAttribute("successMessage", "ärende skapat.");
        } catch (RegistryNotFoundException | UserNotFoundException | IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        }

        populateCaseRecordPanelModel(registryId, model, currentUser);
        return "fragments/case-management/case-record-list :: caseRecordList";
    }

    @GetMapping("/case-records/{caseId}")
    public String caseRecordDetail(@PathVariable Long caseId, Model model) {
        model.addAttribute("caseRecord", caseRecordService.findById(caseId));
        model.addAttribute("caseRecordId", caseId);
        model.addAttribute("assignableUsers", userService.findAll().stream()
                .map(user -> new AssignableUserOption(
                        user.getId().toBase64UrlString(),
                        buildDisplayName(user)
                ))
                .toList());

        return "fragments/case-management/case-record-detail :: caseRecordDetail";
    }

    @PostMapping("/case-records/{caseId}/update")
    public String updateCaseRecord(
            @PathVariable Long caseId,
            @RequestParam String status,
            @RequestParam(required = false) String assignedUserId,
            Model model
    ) {
        try {
            caseRecordService.updateCaseRecord(caseId, status, normalizeAssignedUserId(assignedUserId));
            model.addAttribute("successMessage", "ändringarna sparades.");
        } catch (CaseRecordNotFoundException | UserNotFoundException | IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        }

        model.addAttribute("caseRecord", caseRecordService.findById(caseId));
        model.addAttribute("caseRecordId", caseId);
        model.addAttribute("assignableUsers", userService.findAll().stream()
                .map(user -> new AssignableUserOption(
                        user.getId().toBase64UrlString(),
                        buildDisplayName(user)
                ))
                .toList());

        return "fragments/case-management/case-record-detail :: caseRecordDetail";
    }

    private void populateCaseRecordPanelModel(Long registryId, Model model, UserEntity currentUser) {
        model.addAttribute("registryId", registryId);
        model.addAttribute("registryName", registryService.findById(registryId).name());
        model.addAttribute("caseRecords", caseRecordService.findByRegistryId(registryId));
        model.addAttribute("currentUserDisplayName", buildDisplayName(currentUser));
        model.addAttribute("assignableUsers", userService.findAll().stream()
                .map(user -> new AssignableUserOption(
                        user.getId().toBase64UrlString(),
                        buildDisplayName(user)
                ))
                .toList());
    }

    private String normalizeAssignedUserId(String assignedUserId) {
        if (assignedUserId == null || assignedUserId.isBlank()) {
            return null;
        }
        return assignedUserId;
    }

    private String buildDisplayName(UserEntity user) {
        String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String lastName = user.getLastName() != null ? user.getLastName().trim() : "";

        String fullName = (firstName + " " + lastName).trim();
        if (!fullName.isBlank()) {
            return fullName;
        }

        if (user.getDisplayName() != null && !user.getDisplayName().isBlank()) {
            return user.getDisplayName();
        }

        return user.getName();
    }

    private record AssignableUserOption(String id, String displayName) {
    }
}
