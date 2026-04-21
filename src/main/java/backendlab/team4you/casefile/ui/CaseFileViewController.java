package backendlab.team4you.casefile.ui;

import backendlab.team4you.casefile.CaseFileService;
import backendlab.team4you.common.ConfidentialityLevel;
import backendlab.team4you.exceptions.*;
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
            @RequestParam("confidentialityLevel") ConfidentialityLevel confidentialityLevel,
            Model model,
            Principal principal
    ) {
        UserEntity currentUser = userService.getCurrentUser(principal);

        try {
            caseFileService.uploadFile(caseId, file, confidentialityLevel, currentUser);
            model.addAttribute("successMessage", "Filen laddades upp.");
        } catch (CaseRecordNotFoundException ex) {
            model.addAttribute("errorMessage", "Ärendet kunde inte hittas.");
        } catch (org.springframework.security.access.AccessDeniedException ex) {
            model.addAttribute("errorMessage", "Du har inte behörighet att ladda upp filer här.");
        } catch (InvalidFileNameException | FileTooLargeException | FileStorageConfigurationException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Något gick fel vid uppladdning av filen.");
        }

        return reloadFileListFragment(caseId, currentUser, model);
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
        } catch (CaseRecordNotFoundException ex) {
            model.addAttribute("errorMessage", "Ärendet kunde inte hittas.");
        } catch (CaseFileNotFoundException ex) {
            model.addAttribute("errorMessage", "Filen kunde inte hittas.");
        } catch (org.springframework.security.access.AccessDeniedException ex) {
            model.addAttribute("errorMessage", "Du har inte behörighet att ta bort den här filen.");
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Något gick fel när filen skulle tas bort.");
        }

        return reloadFileListFragment(caseId, currentUser, model);
    }

    private String reloadFileListFragment(Long caseId, UserEntity currentUser, Model model) {
        model.addAttribute("caseRecordId", caseId);

        try {
            model.addAttribute("files", caseFileService.listFileItemsForViewer(caseId, currentUser));
        } catch (Exception ex) {
            model.addAttribute("files", java.util.List.of());
        }

        return "fragments/case-management/case-file-list :: caseFileList";
    }
}
