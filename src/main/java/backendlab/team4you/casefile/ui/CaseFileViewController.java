package backendlab.team4you.casefile.ui;

import backendlab.team4you.audit.AuditAction;
import backendlab.team4you.audit.AuditService;
import backendlab.team4you.casefile.CaseFileService;
import backendlab.team4you.common.ConfidentialityLevel;
import backendlab.team4you.exceptions.CaseFileNotFoundException;
import backendlab.team4you.exceptions.CaseRecordNotFoundException;
import backendlab.team4you.exceptions.FileStorageConfigurationException;
import backendlab.team4you.exceptions.FileTooLargeException;
import backendlab.team4you.exceptions.InvalidFileNameException;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Controller
@RequestMapping("/dashboard/case-management")
public class CaseFileViewController {

    private static final Logger log = LoggerFactory.getLogger(CaseFileViewController.class);

    private final CaseFileService caseFileService;
    private final UserService userService;
    private final AuditService auditService;

    public CaseFileViewController(CaseFileService caseFileService, UserService userService, AuditService auditService) {
        this.caseFileService = caseFileService;
        this.userService = userService;
        this.auditService = auditService;
    }

    @GetMapping("/case-records/{caseId}/files")
    public String caseFiles(@PathVariable Long caseId, Model model, Principal principal) {
        UserEntity currentUser = userService.getCurrentUser(principal);

        model.addAttribute("files", caseFileService.listFileItemsForViewer(caseId, currentUser));
        model.addAttribute("caseRecordId", caseId);
        return "fragments/case-management/case-file-list :: caseFileList";
    }

    @PostMapping("/case-records/{caseId}/files")
    @AuditAction(action = "FILE_UPLOAD_UI", entity = "CASE_FILE")
    public String uploadCaseFile(
            @PathVariable Long caseId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("confidentialityLevel") ConfidentialityLevel confidentialityLevel,
            Model model,
            Principal principal
    ) {
        UserEntity currentUser = userService.getCurrentUser(principal);
        auditService.log("TEST_USER", "MANUAL_LOG", "CASE", caseId, "Testar loggning", "SUCCESS");

        try {
            caseFileService.uploadFile(caseId, file, confidentialityLevel, currentUser);
            model.addAttribute("successMessage", "Filen laddades upp.");
        } catch (CaseRecordNotFoundException ex) {
            model.addAttribute("errorMessage", "Ärendet kunde inte hittas.");
        } catch (org.springframework.security.access.AccessDeniedException ex) {
            model.addAttribute("errorMessage", "Du har inte behörighet att ladda upp filer här.");
        } catch (InvalidFileNameException | FileTooLargeException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
        } catch (FileStorageConfigurationException ex) {
            log.error("File storage configuration error while uploading file for caseId={}", caseId, ex);
            model.addAttribute("errorMessage", "Filhanteringen är tillfälligt otillgänglig.");
        } catch (Exception ex) {
            log.error("Unexpected error while uploading file for caseId={}", caseId, ex);
            model.addAttribute("errorMessage", "Något gick fel vid uppladdning av filen.");
        }

        return reloadFileListFragment(caseId, currentUser, model);
    }

    @DeleteMapping("/case-records/{caseId}/files/{fileId}")
    @ResponseBody
    @AuditAction(action = "FILE_DELETE_UI", entity = "CASE_FILE")
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
            log.error("Unexpected error while deleting fileId={} for caseId={}", fileId, caseId, ex);
            model.addAttribute("errorMessage", "Något gick fel när filen skulle tas bort.");
        }

        return reloadFileListFragment(caseId, currentUser, model);
    }

    private String reloadFileListFragment(Long caseId, UserEntity currentUser, Model model) {
        model.addAttribute("caseRecordId", caseId);

        try {
            model.addAttribute("files", caseFileService.listFileItemsForViewer(caseId, currentUser));
        } catch (Exception ex) {
            log.error("Unexpected error while reloading file list for caseId={}", caseId, ex);
            model.addAttribute("files", java.util.List.of());
        }

        return "fragments/case-management/case-file-list :: caseFileList";
    }
}