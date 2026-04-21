package backendlab.team4you.casefile.ui;

import backendlab.team4you.casefile.CaseFileService;
import backendlab.team4you.casefile.FileConfidentialityLevel;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Controller
@RequestMapping("/dashboard/case-management")
public class CaseFileViewController {

    private final CaseFileService caseFileService;
    private final UserService userService;

    public CaseFileViewController(CaseFileService caseFileService, UserService userService) {
        this.caseFileService = caseFileService;
        this.userService = userService;
    }

    @GetMapping("/case-records/{caseId}/files")
    public String caseFiles(@PathVariable Long caseId, Model model, Principal principal) {
        UserEntity currentUser = userService.getCurrentUser(principal);

        model.addAttribute("files", caseFileService.listFileItemsForViewer(caseId, currentUser));
        model.addAttribute("caseRecordId", caseId);
        return "fragments/case-management/case-file-list :: caseFileList";
    }

    @PostMapping("/case-records/{caseId}/files")
    public String uploadCaseFile(
            @PathVariable Long caseId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("confidentialityLevel") FileConfidentialityLevel confidentialityLevel,
            Model model,
            Principal principal
    ) {
        UserEntity currentUser = userService.getCurrentUser(principal);

        try {
            caseFileService.uploadFile(caseId, file, confidentialityLevel, currentUser);
            model.addAttribute("successMessage", "Filen laddades upp.");
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
        }

        model.addAttribute("files", caseFileService.listFileItemsForViewer(caseId, currentUser));
        model.addAttribute("caseRecordId", caseId);
        return "fragments/case-management/case-file-list :: caseFileList";
    }

    @DeleteMapping("/case-records/{caseId}/files/{fileId}")
    public String deleteCaseFile(
            @PathVariable Long caseId,
            @PathVariable Long fileId,
            Model model,
            Principal principal
    ) {
        UserEntity currentUser = userService.getCurrentUser(principal);

        try {
            caseFileService.deleteFile(caseId, fileId, currentUser);
            model.addAttribute("successMessage", "Filen togs bort.");
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
        }

        model.addAttribute("files", caseFileService.listFileItemsForViewer(caseId, currentUser));
        model.addAttribute("caseRecordId", caseId);
        return "fragments/case-management/case-file-list :: caseFileList";
    }
}
