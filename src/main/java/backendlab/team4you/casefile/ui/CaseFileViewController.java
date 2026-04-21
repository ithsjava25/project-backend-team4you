package backendlab.team4you.casefile.ui;

import backendlab.team4you.casefile.CaseFileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/dashboard/case-management")
public class CaseFileViewController {

    private final CaseFileService caseFileService;

    public CaseFileViewController(CaseFileService caseFileService) {
        this.caseFileService = caseFileService;
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
    ) {
        try {
            caseFileService.uploadFile(caseId, file);
            model.addAttribute("successMessage", "Filen laddades upp.");
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
        }

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
        try {
            caseFileService.deleteFile(caseId, fileId);
            model.addAttribute("successMessage", "Filen togs bort.");
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
        }

        model.addAttribute("files", caseFileService.listFiles(caseId));
        model.addAttribute("caseRecordId", caseId);
        return "fragments/case-management/case-file-list :: caseFileList";
    }
}
