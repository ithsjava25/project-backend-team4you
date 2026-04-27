package backendlab.team4you.protocol;

import backendlab.team4you.casefile.CaseFile;
import backendlab.team4you.exceptions.UserNotFoundException;
import backendlab.team4you.meeting.MeetingRepository;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/protocols")
@PreAuthorize("hasRole('ADMIN')")
public class ProtocolArchiveController {

    private final ProtocolArchiveService protocolArchiveService;
    private final ProtocolRepository protocolRepository;
    private final MeetingRepository meetingRepository;
    private final ProtocolService protocolService;
    private final UserRepository userRepository;

    public ProtocolArchiveController(
            ProtocolArchiveService protocolArchiveService,
            ProtocolRepository protocolRepository,
            MeetingRepository meetingRepository,
            ProtocolService protocolService,
            UserRepository userRepository) {
        this.protocolArchiveService = protocolArchiveService;
        this.protocolRepository = protocolRepository;
        this.meetingRepository = meetingRepository;
        this.protocolService = protocolService;
        this.userRepository = userRepository;
    }

    @PostMapping("/{protocolId}/archive-pdf")
    public String archiveProtocolPdf(
            @PathVariable Long protocolId,
            Authentication authentication,
            Model model
    ) {
        UserEntity currentUser = userRepository.findByName(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("user not found: " + authentication.getName()));

        CaseFile archivedFile = protocolArchiveService.archiveProtocolPdf(protocolId, currentUser);

        Protocol protocol = protocolService.getProtocol(protocolId);

        model.addAttribute(
                "successMessage",
                "Protokollet sparades som PDF i årsärendet som " + archivedFile.getDocumentReference() + "."
        );
        model.addAttribute("selectedProtocol", protocol);
        model.addAttribute(
                "completedMeetingsWithoutProtocol",
                meetingRepository.findCompletedMeetingsWithoutProtocol()
        );
        model.addAttribute("protocols", protocolRepository.findAll());

        return "fragments/admin-protocols :: content";
    }
}
