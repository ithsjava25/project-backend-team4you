package backendlab.team4you.protocol;

import backendlab.team4you.meeting.MeetingRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/protocols")
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
    public String listProtocols(Model model) {
        model.addAttribute(
                "completedMeetingsWithoutProtocol",
                meetingRepository.findCompletedMeetingsWithoutProtocol()
        );
        model.addAttribute("protocols", protocolRepository.findAll());

        return "fragments/admin-protocols :: content";
    }

    @PostMapping("/meetings/{meetingId}")
    public String createProtocol(
            @PathVariable Long meetingId,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        Protocol protocol = protocolService.createProtocolForCompletedMeeting(meetingId);

        model.addAttribute("successMessage", "Protokoll skapades.");
        model.addAttribute("selectedProtocol", protocol);
        model.addAttribute(
                "completedMeetingsWithoutProtocol",
                meetingRepository.findCompletedMeetingsWithoutProtocol()
        );
        model.addAttribute("protocols", protocolRepository.findAll());

        return "fragments/admin-protocols :: content";
    }

    @GetMapping("/{protocolId}")
    public String viewProtocol(
            @PathVariable Long protocolId,
            Model model
    ) {
        Protocol protocol = protocolService.getProtocol(protocolId);

        model.addAttribute("selectedProtocol", protocol);
        model.addAttribute(
                "completedMeetingsWithoutProtocol",
                meetingRepository.findCompletedMeetingsWithoutProtocol()
        );
        model.addAttribute("protocols", protocolRepository.findAll());

        return "fragments/admin-protocols :: content";
    }

    @PostMapping("/paragraphs/{paragraphId}/decision")
    public String updateParagraphDecision(
            @PathVariable Long paragraphId,
            @RequestParam ProtocolDecisionType decisionType,
            @RequestParam String decisionText,
            Model model
    ) {
        Protocol protocol = protocolService.updateParagraphDecision(
                paragraphId,
                decisionType,
                decisionText
        );

        model.addAttribute("successMessage", "Beslut sparades.");
        model.addAttribute("selectedProtocol", protocol);
        model.addAttribute(
                "completedMeetingsWithoutProtocol",
                meetingRepository.findCompletedMeetingsWithoutProtocol()
        );
        model.addAttribute("protocols", protocolRepository.findAll());

        return "fragments/admin-protocols :: content";
    }

    @GetMapping("/paragraphs/{paragraphId}/decision-text")
    public String getDefaultDecisionText(
            @PathVariable Long paragraphId,
            @RequestParam ProtocolDecisionType decisionType,
            Model model
    ) {
        model.addAttribute(
                "decisionText",
                protocolService.buildDefaultDecisionText(paragraphId, decisionType)
        );
        model.addAttribute("paragraphId", paragraphId);

        return "fragments/admin-protocols :: decisionTextField";
    }
}
