package backendlab.team4you.casefile.ui;

import backendlab.team4you.exceptions.DuplicateRegistryCodeException;
import backendlab.team4you.exceptions.DuplicateRegistryNameException;
import backendlab.team4you.registry.RegistryRequestDto;
import backendlab.team4you.registry.RegistryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/dashboard/case-management")
public class CaseManagementViewController {

    @GetMapping
    public String page() {
        return "fragments/case-management/page :: content";
    }
}
