package backendlab.team4you.casefile.ui;

import backendlab.team4you.casefile.CaseFileService;
import backendlab.team4you.caserecord.CaseRecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import backendlab.team4you.registry.RegistryService;


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
    public String page(HttpServletRequest request) {
        if ("true".equals(request.getHeader("HX-Request"))) {
            return "fragments/case-management/page :: content";
        }
        return "dashboard/case-management";
    }

    @GetMapping("/registries")
    public String registries(Model model) {
        model.addAttribute("registries", registryService.findAll());
        return "fragments/case-management/registry-list :: registryList";
    }

    @GetMapping("/registries/{registryId}/case-records")
    public String caseRecords(@PathVariable Long registryId, Model model) {
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
}
