package backendlab.team4you.webauthn;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class AdminController {

    @GetMapping("/admin/logs")
    public String logs(Model model) {
        List<String> logs = List.of(
                "User johndoe loggade in",
                "Ny användare registrerad",
                "Admin tog bort user #123"
        );

        model.addAttribute("logs", logs);
        return "fragments/admin-logs :: content";
    }
}
