package backendlab.team4you.casefile.ui;

import backendlab.team4you.casefile.CaseFileService;
import backendlab.team4you.caserecord.CaseRecordRequestDto;
import backendlab.team4you.caserecord.CaseRecordService;
import backendlab.team4you.exceptions.DuplicateRegistryCodeException;
import backendlab.team4you.exceptions.DuplicateRegistryNameException;
import backendlab.team4you.exceptions.RegistryNotFoundException;
import backendlab.team4you.exceptions.UserNotFoundException;
import backendlab.team4you.registry.RegistryRequestDto;
import backendlab.team4you.registry.RegistryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/dashboard/case-management")
public class CaseManagementViewController {

    private final RegistryService registryService;
    private final CaseRecordService caseRecordService;
    private final CaseFileService caseFileService;

    public CaseManagementViewController(
            RegistryService registryService,
            CaseRecordService caseRecordService,
            CaseFileService caseFileService
    ) {
        this.registryService = registryService;
        this.caseRecordService = caseRecordService;
        this.caseFileService = caseFileService;
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
            registryService.createRegistry(new RegistryRequestDto(name, code));
            model.addAttribute("successMessage", "Registry skapad.");
        } catch (DuplicateRegistryNameException
                 | DuplicateRegistryCodeException
                 | IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        }
        model.addAttribute("registries", registryService.findAll());
        return "fragments/case-management/registry-list :: registryList";
    }

    @GetMapping("/registries/{registryId}/case-records")
    public String caseRecords(@PathVariable Long registryId, Model model) {
        model.addAttribute("registryId", registryId);
        model.addAttribute("caseRecords", caseRecordService.findByRegistryId(registryId));
        return "fragments/case-management/case-record-list :: caseRecordList";
    }

    @PostMapping("/registries/{registryId}/case-records")
    public String createCaseRecord(
            @PathVariable Long registryId,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam String status,
            @RequestParam String ownerUserId,
            @RequestParam String assignedUserId,
            @RequestParam String confidentialityLevel,
            Model model
    ) {
        try {
            CaseRecordRequestDto requestDto = new CaseRecordRequestDto(
                    registryId,
                    title,
                    description,
                    status,
                    ownerUserId,
                    assignedUserId,
                    confidentialityLevel,
                    null
            );

            caseRecordService.createCaseRecord(requestDto);
            model.addAttribute("successMessage", "Ärende skapat.");
        } catch (RegistryNotFoundException | UserNotFoundException | IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        }

        model.addAttribute("registryId", registryId);
        model.addAttribute("caseRecords", caseRecordService.findByRegistryId(registryId));
        return "fragments/case-management/case-record-list :: caseRecordList";
    }

    @GetMapping("/case-records/{caseId}")
    public String caseRecordDetail(@PathVariable Long caseId, Model model) {
        model.addAttribute("caseRecord", caseRecordService.findById(caseId));
        model.addAttribute("caseRecordId", caseId);
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
}
