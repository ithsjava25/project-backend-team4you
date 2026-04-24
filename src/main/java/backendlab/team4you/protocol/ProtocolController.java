package backendlab.team4you.protocol;

import backendlab.team4you.meeting.Meeting;
import backendlab.team4you.meeting.MeetingRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/dashboard/admin/protocols")
@PreAuthorize("hasRole('ADMIN')")
public class ProtocolController {

    private final ProtocolService protocolService;
    private final ProtocolRepository protocolRepository;
    private final MeetingRepository meetingRepository;

    public ProtocolController(
            ProtocolService protocolService,
            ProtocolRepository protocolRepository,
            MeetingRepository meetingRepository
    ) {
        this.protocolService = protocolService;
        this.protocolRepository = protocolRepository;
        this.meetingRepository = meetingRepository;
    }

    @GetMapping
    public String listProtocolOptions(Model model) {
        List<Meeting> completedMeetingsWithoutProtocol =
                meetingRepository.findCompletedMeetingsWithoutProtocol();

        List<Protocol> protocols = protocolRepository.findAll();

        model.addAttribute("completedMeetingsWithoutProtocol", completedMeetingsWithoutProtocol);
        model.addAttribute("protocols", protocols);

        return "dashboard/admin/protocols";
    }

    @PostMapping("/meetings/{meetingId}")
    public String createProtocol(
            @PathVariable Long meetingId,
            RedirectAttributes redirectAttributes
    ) {
        Protocol protocol = protocolService.createProtocolForCompletedMeeting(meetingId);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Protokoll skapades."
        );

        return "redirect:/dashboard/admin/protocols/" + protocol.getId();
    }

    @GetMapping("/{protocolId}")
    public String viewProtocol(
            @PathVariable Long protocolId,
            Model model
    ) {
        Protocol protocol = protocolService.getProtocol(protocolId);

        model.addAttribute("protocol", protocol);

        return "dashboard/admin/protocol-detail";
    }
}
