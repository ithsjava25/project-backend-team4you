package backendlab.team4you.registryaccess;


import backendlab.team4you.user.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/registry-access")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRegistryAccessController {

    private final RegistryAccessService registryAccessService;
    private final UserRepository userRepository;

    public AdminRegistryAccessController(
            RegistryAccessService registryAccessService,
            UserRepository userRepository
    ) {
        this.registryAccessService = registryAccessService;
        this.userRepository = userRepository;
    }


    @GetMapping
    public String viewRegistryAccess(
            Model model,
            @RequestHeader(
                    value = "HX-Request",
                    required = false
            ) String htmx
    ) {

        model.addAttribute(
                "users",
                userRepository.findAll()
        );

        if (htmx != null) {
            return "fragments/admin-registry-access :: content";
        }

        return "fragments/admin-registry-access";
    }

    @GetMapping("/user/{userId}")
    public String getUserRegistryPermissions(
            @PathVariable String userId,
            Model model
    ) {

        model.addAttribute(
                "permissions",
                registryAccessService
                        .getRegistryPermissionsForUser(userId)
        );

        model.addAttribute(
                "selectedUser",
                userRepository.findById(userId)
                        .orElseThrow()
        );

        return "fragments/registry-user-permissions :: content";
    }



}
