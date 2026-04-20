package backendlab.team4you.casefile.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard/case-management")
public class CaseManagementViewController {

    @GetMapping
    public String caseManagementPage() {
        return "dashboard/case-management";
    }
}
