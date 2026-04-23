package backendlab.team4you.meeting;

import backendlab.team4you.caserecord.CaseRecord;
import backendlab.team4you.caserecord.CaseRecordRepository;
import backendlab.team4you.registry.Registry;
import backendlab.team4you.registry.RegistryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

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
        populateMeetingsPage(model, registryId, selectedMeetingId);
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
            @RequestHeader(value = "HX-Request", required = false) String htmx,
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

            model.addAttribute("successMessage", "sammanträdet skapades.");
            populateMeetingsPage(model, registryId, meeting.getId());

        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            populateMeetingsPage(model, registryId, null);
        }

        if (htmx != null) {
            return "fragments/admin-meetings :: content";
        }

        return "admin/meetings";
    }

    @GetMapping("/{meetingId}")
    public String showMeeting(
            @PathVariable Long meetingId,
            @RequestHeader(value = "HX-Request", required = false) String htmx,
            Model model
    ) {
        Meeting meeting = meetingService.getMeetingById(meetingId);
        populateMeetingsPage(model, meeting.getRegistry().getId(), meetingId);

        if (htmx != null) {
            return "fragments/admin-meetings :: content";
        }

        return "admin/meetings";
    }

    @PostMapping("/{meetingId}/agenda-items")
    public String addAgendaItem(
            @PathVariable Long meetingId,
            @RequestParam Long caseRecordId,
            @RequestHeader(value = "HX-Request", required = false) String htmx,
            Model model
    ) {
        Meeting meeting = meetingService.getMeetingById(meetingId);

        try {
            meetingService.addCaseRecordToMeeting(meetingId, caseRecordId);
            model.addAttribute("successMessage", "ärendet lades till på sammanträdet.");
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        }

        populateMeetingsPage(model, meeting.getRegistry().getId(), meetingId);

        if (htmx != null) {
            return "fragments/admin-meetings :: content";
        }

        return "admin/meetings";
    }

    @DeleteMapping("/{meetingId}/agenda-items/{agendaItemId}")
    public String removeAgendaItem(
            @PathVariable Long meetingId,
            @PathVariable Long agendaItemId,
            @RequestHeader(value = "HX-Request", required = false) String htmx,
            Model model
    ) {
        Meeting meeting = meetingService.getMeetingById(meetingId);

        try {
            meetingService.removeAgendaItem(meetingId, agendaItemId);
            model.addAttribute("successMessage", "dagordningspunkten togs bort.");
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        }

        populateMeetingsPage(model, meeting.getRegistry().getId(), meetingId);

        if (htmx != null) {
            return "fragments/admin-meetings :: content";
        }

        return "admin/meetings";
    }

    @PostMapping("/{meetingId}/agenda-items/{agendaItemId}/documents")
    public String addAgendaDocument(
            @PathVariable Long meetingId,
            @PathVariable Long agendaItemId,
            @RequestParam Long caseFileId,
            Model model
    ) {
        Meeting meeting = meetingService.getMeetingById(meetingId);

        try {
            meetingService.addDocumentToAgendaItem(meetingId, agendaItemId, caseFileId);
            model.addAttribute("successMessage", "Handlingen lades till som beslutsunderlag.");
        } catch (Exception exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        }

        populateMeetingsPage(model, meeting.getRegistry().getId(), meetingId);
        return "fragments/admin-meetings :: content";
    }

    @PostMapping("/{meetingId}/agenda-items/{agendaItemId}/documents/{documentId}/remove")
    public String removeAgendaDocument(
            @PathVariable Long meetingId,
            @PathVariable Long agendaItemId,
            @PathVariable Long documentId,
            Model model
    ) {
        Meeting meeting = meetingService.getMeetingById(meetingId);

        try {
            meetingService.removeDocumentFromAgendaItem(meetingId, agendaItemId, documentId);
            model.addAttribute("successMessage", "Handlingen togs bort från beslutsunderlaget.");
        } catch (Exception exception) {
            model.addAttribute("errorMessage", exception.getMessage());
        }

        populateMeetingsPage(model, meeting.getRegistry().getId(), meetingId);
        return "fragments/admin-meetings :: content";
    }

    private void populateMeetingsPage(Model model, Long registryId, Long selectedMeetingId) {
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
    }
}
