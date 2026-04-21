package backendlab.team4you.casefile.ui;

import backendlab.team4you.casefile.CaseFileService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/dashboard/case-management/case-records/{caseId}/files")
public class CaseFileDashboardController {

    private final CaseFileService caseFileService;

    public CaseFileDashboardController(CaseFileService caseFileService) {
        this.caseFileService = caseFileService;
    }

    @GetMapping
    public String getFiles(@PathVariable Long caseId, Model model) {
        model.addAttribute("caseRecordId", caseId);
        model.addAttribute("files", caseFileService.listFiles(caseId));
        return "fragments/case-file-list :: caseFileList";
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadFile(@PathVariable Long caseId,
                             @RequestParam("file") MultipartFile file,
                             Model model) {
        try {
            caseFileService.uploadFile(caseId, file);
            model.addAttribute("successMessage", "filen laddades upp");
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        model.addAttribute("caseRecordId", caseId);
        model.addAttribute("files", caseFileService.listFiles(caseId));
        return "fragments/case-file-list :: caseFileList";
    }

    @DeleteMapping("/{fileId}")
    public String deleteFile(@PathVariable Long caseId,
                             @PathVariable Long fileId,
                             Model model) {
        try {
            caseFileService.deleteFile(caseId, fileId);
            model.addAttribute("successMessage", "filen togs bort");
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        model.addAttribute("caseRecordId", caseId);
        model.addAttribute("files", caseFileService.listFiles(caseId));
        return "fragments/case-file-list :: caseFileList";
    }
}
