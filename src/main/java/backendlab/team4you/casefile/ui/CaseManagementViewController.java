package backendlab.team4you.casefile.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class CaseManagementViewController {

    @GetMapping("/case-management")
    public String caseManagementFragment() {
        return "fragments/case-management/page :: content";
    }

    @GetMapping("/case-management-page")
    public String caseManagementPage() {
        return "dashboard/case-management";
    }
}
