package backendlab.team4you.casefile.ui;

import backendlab.team4you.casefile.CaseFileService;
import backendlab.team4you.caserecord.CaseRecordRequestDto;
import backendlab.team4you.caserecord.CaseRecordService;
import backendlab.team4you.exceptions.*;
import backendlab.team4you.registry.RegistryRequestDto;
import backendlab.team4you.registry.RegistryService;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@Controller
@RequestMapping("/dashboard/case-management")
public class CaseManagementViewController {

    private final RegistryService registryService;
    private final CaseRecordService caseRecordService;
    private final CaseFileService caseFileService;
    private final UserService userService;

    public CaseManagementViewController(
            RegistryService registryService,
            CaseRecordService caseRecordService,
            CaseFileService caseFileService,
            UserService userService
    ) {
        this.registryService = registryService;
        this.caseRecordService = caseRecordService;
        this.caseFileService = caseFileService;
        this.userService = userService;
    }

    @GetMapping
    public String page() {
        return "fragments/case-management/page :: content";
    }

    @GetMapping("/registries")
    public String registries(Model model) {
        model.addAttribute("registries", registryService.findAll());
        return "fragments/case-management/registry-list :: registryList";
    }

    @PostMapping("/registries")
    public String createRegistry(
            @RequestParam String name,
            @RequestParam String code,
            Model model
    ) {
        try {
            registryService.createRegistry(new RegistryRequestDto(name.trim(), code.trim()));
            model.addAttribute("successMessage", "registry skapad.");
        } catch (DuplicateRegistryNameException
                 | DuplicateRegistryCodeException
                 | IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        }

        model.addAttribute("registries", registryService.findAll());
        return "fragments/case-management/registry-list :: registryList";
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

    @GetMapping("/case-records/{caseId}/files")
    public String caseFiles(@PathVariable Long caseId, Model model) {
        model.addAttribute("files", caseFileService.listFiles(caseId));
        model.addAttribute("caseRecordId", caseId);
        return "fragments/case-management/case-file-list :: caseFileList";
    }

    @PostMapping("/case-records/{caseId}/files")
    public String uploadCaseFile(
            @PathVariable Long caseId,
            @RequestParam("file") MultipartFile file,
            Model model
    ) throws IOException {
        caseFileService.uploadFile(caseId, file);
        model.addAttribute("files", caseFileService.listFiles(caseId));
        model.addAttribute("caseRecordId", caseId);
        return "fragments/case-management/case-file-list :: caseFileList";
    }

    @DeleteMapping("/case-records/{caseId}/files/{fileId}")
    public String deleteCaseFile(
            @PathVariable Long caseId,
            @PathVariable Long fileId,
            Model model
    ) {
        caseFileService.deleteFile(caseId, fileId);
        model.addAttribute("files", caseFileService.listFiles(caseId));
        model.addAttribute("caseRecordId", caseId);
        return "fragments/case-management/case-file-list :: caseFileList";
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

    @PostMapping("/case-records/{caseId}/update")
    public String updateCaseRecord(
            @PathVariable Long caseId,
            @RequestParam String status,
            @RequestParam(required = false) String assignedUserId,
            Model model
    ) {
        try {
            caseRecordService.updateCaseRecord(caseId, status, normalizeAssignedUserId(assignedUserId));
            model.addAttribute("successMessage", "ärendet uppdaterades.");
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
}