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
public class RegistryViewController {

    private final RegistryService registryService;

    public RegistryViewController(RegistryService registryService) {
        this.registryService = registryService;
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
            registryService.createRegistry(new RegistryRequestDto(name.trim(), code.trim()));
            model.addAttribute("successMessage", "registry skapad.");
        } catch (DuplicateRegistryNameException
                 | DuplicateRegistryCodeException
                 | IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        }

        model.addAttribute("registries", registryService.findAll());
        return "fragments/case-management/registry-list :: registryList";
    }
}
