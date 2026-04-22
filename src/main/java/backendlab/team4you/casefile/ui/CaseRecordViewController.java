package backendlab.team4you.casefile.ui;

import backendlab.team4you.caserecord.CaseRecordRequestDto;
import backendlab.team4you.caserecord.CaseRecordService;
import backendlab.team4you.caserecord.CaseStatus;
import backendlab.team4you.common.ConfidentialityLevel;
import backendlab.team4you.exceptions.CaseRecordNotFoundException;
import backendlab.team4you.exceptions.RegistryNotFoundException;
import backendlab.team4you.exceptions.UserNotFoundException;
import backendlab.team4you.registry.RegistryService;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/dashboard/case-management")
public class CaseRecordViewController {

    private static final Logger log = LoggerFactory.getLogger(CaseRecordViewController.class);

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
        return reloadCaseRecordListFragment(registryId, model, currentUser);
    }

    @PostMapping("/registries/{registryId}/case-records")
    public String createCaseRecord(
            @PathVariable Long registryId,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam CaseStatus status,
            @RequestParam(required = false) String assignedUserId,
            @RequestParam ConfidentialityLevel confidentialityLevel,
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
        } catch (RegistryNotFoundException exception) {
            model.addAttribute("errorMessage", "Registriet kunde inte hittas.");
            return buildMissingRegistryFragment(registryId, model, currentUser);
        } catch (UserNotFoundException | IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        } catch (Exception exception) {
            log.error("Unexpected error while creating case record for registryId={}", registryId, exception);
            model.addAttribute("errorMessage", "Något gick fel när ärendet skulle skapas.");
        }

        return reloadCaseRecordListFragment(registryId, model, currentUser);
    }

    @GetMapping("/case-records/{caseId}")
    public String caseRecordDetail(@PathVariable Long caseId, Model model) {
        try {
            populateCaseRecordDetailModel(caseId, model);
        } catch (CaseRecordNotFoundException exception) {
            model.addAttribute("errorMessage", "Ärendet kunde inte hittas.");
            return buildMissingCaseRecordDetailFragment(caseId, model);
        } catch (Exception exception) {
            log.error("Unexpected error while loading case record detail for caseId={}", caseId, exception);
            model.addAttribute("errorMessage", "Något gick fel när ärendet skulle laddas.");
            return buildMissingCaseRecordDetailFragment(caseId, model);
        }

        return "fragments/case-management/case-record-detail :: caseRecordDetail";
    }

    @PostMapping("/case-records/{caseId}/update")
    public String updateCaseRecord(
            @PathVariable Long caseId,
            @RequestParam CaseStatus status,
            @RequestParam(required = false) String assignedUserId,
            Model model
    ) {
        try {
            caseRecordService.updateCaseRecord(caseId, status, normalizeAssignedUserId(assignedUserId));
            model.addAttribute("successMessage", "ändringarna sparades.");
        } catch (CaseRecordNotFoundException exception) {
            model.addAttribute("errorMessage", "Ärendet kunde inte hittas.");
            return buildMissingCaseRecordDetailFragment(caseId, model);
        } catch (UserNotFoundException | IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        } catch (Exception exception) {
            log.error("Unexpected error while updating case record caseId={}", caseId, exception);
            model.addAttribute("errorMessage", "Något gick fel när ärendet skulle uppdateras.");
        }

        return reloadCaseRecordDetailFragment(caseId, model);
    }

    private String reloadCaseRecordListFragment(Long registryId, Model model, UserEntity currentUser) {
        try {
            populateCaseRecordPanelModel(registryId, model, currentUser);
        } catch (RegistryNotFoundException exception) {
            model.addAttribute("errorMessage", "Registret kunde inte hittas.");
            return buildMissingRegistryFragment(registryId, model, currentUser);
        } catch (Exception exception) {
            log.error("Unexpected error while reloading case record list for registryId={}", registryId, exception);
            model.addAttribute("errorMessage", "Något gick fel när ärendelistan skulle laddas.");
            return buildFallbackCaseRecordListFragment(registryId, model, currentUser);
        }

        return "fragments/case-management/case-record-list :: caseRecordList";
    }

    private String reloadCaseRecordDetailFragment(Long caseId, Model model) {
        try {
            populateCaseRecordDetailModel(caseId, model);
        } catch (CaseRecordNotFoundException exception) {
            model.addAttribute("errorMessage", "Ärendet kunde inte hittas.");
            return buildMissingCaseRecordDetailFragment(caseId, model);
        } catch (Exception exception) {
            log.error("Unexpected error while reloading case record detail for caseId={}", caseId, exception);
            model.addAttribute("errorMessage", "Något gick fel när ärendedetaljer skulle laddas.");
            return buildMissingCaseRecordDetailFragment(caseId, model);
        }

        return "fragments/case-management/case-record-detail :: caseRecordDetail";
    }

    private String buildMissingRegistryFragment(Long registryId, Model model, UserEntity currentUser) {
        model.addAttribute("registryId", registryId);
        model.addAttribute("registryName", "okänt register");
        model.addAttribute("caseRecords", List.of());
        model.addAttribute("currentUserDisplayName", buildDisplayName(currentUser));
        model.addAttribute("assignableUsers", buildAssignableUserOptions());
        return "fragments/case-management/case-record-list :: caseRecordList";
    }

    private String buildFallbackCaseRecordListFragment(Long registryId, Model model, UserEntity currentUser) {
        model.addAttribute("registryId", registryId);
        model.addAttribute("registryName", "ärenden");
        model.addAttribute("caseRecords", List.of());
        model.addAttribute("currentUserDisplayName", buildDisplayName(currentUser));
        model.addAttribute("assignableUsers", buildAssignableUserOptions());
        return "fragments/case-management/case-record-list :: caseRecordList";
    }

    private String buildMissingCaseRecordDetailFragment(Long caseId, Model model) {
        model.addAttribute("caseRecordId", caseId);
        model.addAttribute("caseRecord", null);
        model.addAttribute("assignableUsers", buildAssignableUserOptions());
        return "fragments/case-management/case-record-detail :: caseRecordDetail";
    }

    private void populateCaseRecordPanelModel(Long registryId, Model model, UserEntity currentUser) {
        model.addAttribute("registryId", registryId);
        model.addAttribute("registryName", registryService.findById(registryId).name());
        model.addAttribute("caseRecords", caseRecordService.findByRegistryId(registryId));
        model.addAttribute("currentUserDisplayName", buildDisplayName(currentUser));
        model.addAttribute("assignableUsers", buildAssignableUserOptions());
    }

    private void populateCaseRecordDetailModel(Long caseId, Model model) {
        model.addAttribute("caseRecord", caseRecordService.findById(caseId));
        model.addAttribute("caseRecordId", caseId);
        model.addAttribute("assignableUsers", buildAssignableUserOptions());
    }

    private List<AssignableUserOption> buildAssignableUserOptions() {
        return userService.findAll().stream()
                .map(user -> new AssignableUserOption(
                        user.getId().toBase64UrlString(),
                        buildDisplayName(user)
                ))
                .toList();
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
