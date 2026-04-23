package backendlab.team4you.meeting;

import backendlab.team4you.caserecord.CaseRecord;
import backendlab.team4you.caserecord.CaseRecordRepository;
import backendlab.team4you.registry.Registry;
import backendlab.team4you.registry.RegistryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
@RequestMapping("/admin/meetings")
public class MeetingController {

    private final MeetingService meetingService;
    private final RegistryRepository registryRepository;
    private final CaseRecordRepository caseRecordRepository;

    public MeetingController(
            MeetingService meetingService,
            RegistryRepository registryRepository,
            CaseRecordRepository caseRecordRepository
    ) {
        this.meetingService = meetingService;
        this.registryRepository = registryRepository;
        this.caseRecordRepository = caseRecordRepository;
    }

    @GetMapping
    public String meetingsPage(
            @RequestParam(required = false) Long registryId,
            @RequestParam(required = false) Long selectedMeetingId,
            Model model
    ) {
        List<Registry> registries = registryRepository.findAll();
        model.addAttribute("registries", registries);
        model.addAttribute("selectedRegistryId", registryId);

        List<Meeting> meetings = registryId != null
                ? meetingService.getMeetingsForRegistry(registryId)
                : List.of();

        model.addAttribute("meetings", meetings);

        Meeting selectedMeeting = null;
        List<MeetingAgendaItem> agendaItems = List.of();
        List<CaseRecord> availableCaseRecords = List.of();

        if (selectedMeetingId != null) {
            selectedMeeting = meetingService.getMeetingById(selectedMeetingId);
            agendaItems = meetingService.getAgendaItems(selectedMeetingId);

            Long selectedRegistryId = selectedMeeting.getRegistry().getId();
            availableCaseRecords = caseRecordRepository.findByRegistryIdOrderByCreatedAtDesc(selectedRegistryId);
            model.addAttribute("selectedRegistryId", selectedRegistryId);
        }

        model.addAttribute("selectedMeeting", selectedMeeting);
        model.addAttribute("agendaItems", agendaItems);
        model.addAttribute("availableCaseRecords", availableCaseRecords);

        return "fragments/admin-meetings :: content";
    }

    @PostMapping
    public String createMeeting(
            @RequestParam Long registryId,
            @RequestParam String title,
            @RequestParam String startsAt,
            @RequestParam(required = false) String endsAt,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String notes,
            Model model
    ) {
        try {
            LocalDateTime parsedStartsAt = LocalDateTime.parse(startsAt);
            LocalDateTime parsedEndsAt = (endsAt == null || endsAt.isBlank())
                    ? null
                    : LocalDateTime.parse(endsAt);

            Meeting meeting = meetingService.createMeeting(
                    registryId,
                    title,
                    parsedStartsAt,
                    parsedEndsAt,
                    location,
                    notes
            );

            model.addAttribute("successMessage", "Sammanträdet skapades.");
            return meetingsPage(registryId, meeting.getId(), model);

        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            return meetingsPage(registryId, null, model);
        }
    }

    @GetMapping("/{meetingId}")
    public String showMeeting(
            @PathVariable Long meetingId,
            Model model
    ) {
        Meeting meeting = meetingService.getMeetingById(meetingId);

        List<Registry> registries = registryRepository.findAll();
        List<Meeting> meetings = meetingService.getMeetingsForRegistry(meeting.getRegistry().getId());
        List<MeetingAgendaItem> agendaItems = meetingService.getAgendaItems(meetingId);
        List<CaseRecord> availableCaseRecords =
                caseRecordRepository.findByRegistryIdOrderByCreatedAtDesc(meeting.getRegistry().getId());

        model.addAttribute("registries", registries);
        model.addAttribute("selectedRegistryId", meeting.getRegistry().getId());
        model.addAttribute("meetings", meetings);
        model.addAttribute("selectedMeeting", meeting);
        model.addAttribute("agendaItems", agendaItems);
        model.addAttribute("availableCaseRecords", availableCaseRecords);

        return "fragments/admin-meetings :: content";
    }

    @PostMapping("/{meetingId}/agenda-items")
    public String addAgendaItem(
            @PathVariable Long meetingId,
            @RequestParam Long caseRecordId,
            Model model
    ) {
        Meeting meeting = meetingService.getMeetingById(meetingId);

        try {
            meetingService.addCaseRecordToMeeting(meetingId, caseRecordId);
            model.addAttribute("successMessage", "Ärendet lades till på sammanträdet.");
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        }

        return showMeeting(meetingId, model);
    }

    @DeleteMapping("/{meetingId}/agenda-items/{agendaItemId}")
    public String removeAgendaItem(
            @PathVariable Long meetingId,
            @PathVariable Long agendaItemId,
            Model model
    ) {
        try {
            meetingService.removeAgendaItem(meetingId, agendaItemId);
            model.addAttribute("successMessage", "Dagordningspunkten togs bort.");
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        }

        return showMeeting(meetingId, model);
    }
}
