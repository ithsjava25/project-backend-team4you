package backendlab.team4you.protocol;

import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/dashboard/protocols")
public class DashboardProtocolController {

    private final ProtocolRepository protocolRepository;
    private final ProtocolService protocolService;
    private final ProtocolViewService protocolViewService;
    private final UserService userService;

    public DashboardProtocolController(
            ProtocolRepository protocolRepository,
            ProtocolService protocolService,
            ProtocolViewService protocolViewService,
            UserService userService
    ) {
        this.protocolRepository = protocolRepository;
        this.protocolService = protocolService;
        this.protocolViewService = protocolViewService;
        this.userService = userService;
    }

    @GetMapping
    public String listProtocols(Model model) {
        model.addAttribute("protocols", protocolRepository.findAll());
        return "fragments/dashboard-protocols :: content";
    }

    @GetMapping("/{protocolId}")
    public String viewProtocol(
            @PathVariable Long protocolId,
            Principal principal,
            Model model
    ) {
        UserEntity currentUser = userService.getCurrentUser(principal);

        model.addAttribute("protocols", protocolRepository.findAll());
        model.addAttribute("selectedProtocol", protocolService.getProtocol(protocolId));
        model.addAttribute(
                "paragraphViews",
                protocolViewService.getParagraphsForViewer(protocolId, currentUser)
        );

        return "fragments/dashboard-protocols :: content";
    }
}
