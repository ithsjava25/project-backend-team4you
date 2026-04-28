package backendlab.team4you.protocol;

import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/protocols")
public class ProtocolViewController {

    private final ProtocolService protocolService;
    private final ProtocolViewService protocolViewService;
    private final UserService userService;

    public ProtocolViewController(
            ProtocolService protocolService,
            ProtocolViewService protocolViewService,
            UserService userService
    ) {
        this.protocolService = protocolService;
        this.protocolViewService = protocolViewService;
        this.userService = userService;
    }

    @GetMapping("/{protocolId}")
    public String viewProtocol(
            @PathVariable Long protocolId,
            Principal principal,
            Model model
    ) {
        UserEntity currentUser = userService.getCurrentUser(principal);

        model.addAttribute("protocol", protocolService.getProtocol(protocolId));
        model.addAttribute(
                "paragraphs",
                protocolViewService.getParagraphsForViewer(protocolId, currentUser)
        );

        return "protocol-view";
    }
}
