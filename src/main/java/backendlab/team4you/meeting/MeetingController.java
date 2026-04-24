package backendlab.team4you.meeting;

import backendlab.team4you.casefile.CaseFile;
import backendlab.team4you.caserecord.CaseRecord;
import backendlab.team4you.caserecord.CaseRecordRepository;
import backendlab.team4you.exceptions.*;
import backendlab.team4you.registry.Registry;
import backendlab.team4you.registry.RegistryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        } catch (InvalidMeetingStateException | MeetingNotFoundException | RegistryNotFoundException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            // If registryId was the invalid input, fall back to the all-meetings listing.
            Long safeRegistryId = null;
            if (registryId != null) {
                try {
                    registryRepository.findById(registryId).ifPresent(r -> {});
                    safeRegistryId = registryId;
                    } catch (Exception ignored) {
                    safeRegistryId = null;
                    }
                }
            populateMeetingsPage(model, safeRegistryId, null);
        }

        if (htmx != null) {
            return "fragments/admin-meetings :: content";
        }

        return "admin/meetings";
    }

    @PostMapping("/{meetingId}/update")
    public String updateMeeting(
            @PathVariable Long meetingId,
            @RequestParam String title,
            @RequestParam String startsAt,
            @RequestParam(required = false) String endsAt,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String notes,
            @RequestParam MeetingStatus status,
            Model model
    ) {
        try {
            LocalDateTime parsedStartsAt = LocalDateTime.parse(startsAt);
            LocalDateTime parsedEndsAt = (endsAt == null || endsAt.isBlank())
                    ? null
                    : LocalDateTime.parse(endsAt);

            Meeting updatedMeeting = meetingService.updateMeeting(
                    meetingId,
                    title,
                    parsedStartsAt,
                    parsedEndsAt,
                    location,
                    notes,
                    status
            );

            model.addAttribute("successMessage", "sammanträdet uppdaterades.");
            populateMeetingsPage(model, updatedMeeting.getRegistry().getId(), updatedMeeting.getId());

        } catch (InvalidMeetingStateException | MeetingNotFoundException | RegistryNotFoundException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            Long registryId = null;
            try {
                registryId = meetingService.getMeetingById(meetingId).getRegistry().getId();
                } catch (MeetingNotFoundException ignored) {
                // meeting no longer exists — fall back to the generic listing
                        }
            populateMeetingsPage(model, registryId, registryId == null ? null : meetingId);
            }

        return "fragments/admin-meetings :: content";
    }

    @PostMapping("/{meetingId}/delete")
    public String deleteMeeting(
            @PathVariable Long meetingId,
            Model model
    ) {
        try {
            Meeting meeting = meetingService.getMeetingById(meetingId);
            Long registryId = meeting.getRegistry().getId();

            meetingService.deleteMeeting(meetingId);

            model.addAttribute("successMessage", "sammanträdet togs bort.");
            populateMeetingsPage(model, registryId, null);

        } catch (Exception exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            populateMeetingsPage(model, null, null);
        }

        return "fragments/admin-meetings :: content";
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
        try {
            Meeting meeting = meetingService.getMeetingById(meetingId);

            meetingService.addCaseRecordToMeeting(meetingId, caseRecordId);
            model.addAttribute("successMessage", "Ärendet lades till på sammanträdet.");

            populateMeetingsPageAfterMeetingAction(model, meeting.getRegistry().getId(), meetingId);

        } catch (DuplicateMeetingAgendaItemException |
                 InvalidMeetingStateException |
                 MeetingNotFoundException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            populateMeetingsPageAfterMeetingAction(model, null, null);
        }

        if (htmx != null) {
            return "fragments/admin-meetings :: content";
        }

        return "admin/meetings";
    }

    @PostMapping("/{meetingId}/agenda-items/{agendaItemId}/move-up")
    public String moveAgendaItemUp(
            @PathVariable Long meetingId,
            @PathVariable Long agendaItemId,
            Model model
    ) {
        try {
            Meeting meeting = meetingService.getMeetingById(meetingId);

            meetingService.moveAgendaItemUp(meetingId, agendaItemId);
            model.addAttribute("successMessage", "Dagordningspunkten flyttades upp.");

            populateMeetingsPageAfterMeetingAction(model, meeting.getRegistry().getId(), meetingId);

        } catch (Exception exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            populateMeetingsPageAfterMeetingAction(model, null, null);
        }

        return "fragments/admin-meetings :: content";
    }

    @PostMapping("/{meetingId}/agenda-items/{agendaItemId}/move-down")
    public String moveAgendaItemDown(
            @PathVariable Long meetingId,
            @PathVariable Long agendaItemId,
            Model model
    ) {
        try {
            Meeting meeting = meetingService.getMeetingById(meetingId);

            meetingService.moveAgendaItemDown(meetingId, agendaItemId);
            model.addAttribute("successMessage", "Dagordningspunkten flyttades ner.");

            populateMeetingsPageAfterMeetingAction(model, meeting.getRegistry().getId(), meetingId);

        } catch (Exception exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            populateMeetingsPageAfterMeetingAction(model, null, null);
        }

        return "fragments/admin-meetings :: content";
    }

    @DeleteMapping("/{meetingId}/agenda-items/{agendaItemId}")
    public String removeAgendaItem(
            @PathVariable Long meetingId,
            @PathVariable Long agendaItemId,
            @RequestHeader(value = "HX-Request", required = false) String htmx,
            Model model
    ) {
        try {
            Meeting meeting = meetingService.getMeetingById(meetingId);

            meetingService.removeAgendaItem(meetingId, agendaItemId);
            model.addAttribute("successMessage", "Dagordningspunkten togs bort.");

            populateMeetingsPageAfterMeetingAction(model, meeting.getRegistry().getId(), meetingId);

        } catch (InvalidMeetingStateException |
                 MeetingAgendaItemNotFoundException |
                 MeetingNotFoundException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            populateMeetingsPageAfterMeetingAction(model, null, null);
        }

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
        try {
            Meeting meeting = meetingService.getMeetingById(meetingId);

            meetingService.addDocumentToAgendaItem(meetingId, agendaItemId, caseFileId);
            model.addAttribute("successMessage", "Handlingen lades till som beslutsunderlag.");

            populateMeetingsPageAfterMeetingAction(model, meeting.getRegistry().getId(), meetingId);

        } catch (Exception exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            populateMeetingsPageAfterMeetingAction(model, null, null);
        }

        return "fragments/admin-meetings :: content";
    }

    @PostMapping("/{meetingId}/agenda-items/{agendaItemId}/documents/{documentId}/remove")
    public String removeAgendaDocument(
            @PathVariable Long meetingId,
            @PathVariable Long agendaItemId,
            @PathVariable Long documentId,
            Model model
    ) {
        try {
            Meeting meeting = meetingService.getMeetingById(meetingId);

            meetingService.removeDocumentFromAgendaItem(meetingId, agendaItemId, documentId);
            model.addAttribute("successMessage", "Handlingen togs bort från beslutsunderlaget.");

            populateMeetingsPageAfterMeetingAction(model, meeting.getRegistry().getId(), meetingId);

        } catch (Exception exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            populateMeetingsPageAfterMeetingAction(model, null, null);
        }

        return "fragments/admin-meetings :: content";
    }

    private void populateMeetingsPageAfterMeetingAction(Model model, Long registryId, Long meetingId) {
        try {
            populateMeetingsPage(model, registryId, meetingId);
        } catch (MeetingNotFoundException exception) {
            populateMeetingsPage(model, null, null);
        }
    }

    private void populateMeetingsPage(Model model, Long registryId, Long selectedMeetingId) {
        List<Registry> registries = registryRepository.findAll();
        model.addAttribute("registries", registries);
        model.addAttribute("selectedRegistryId", registryId);

        List<Meeting> meetings = registryId != null
                ? meetingService.getMeetingsForRegistry(registryId)
                : meetingService.getAllMeetings();

        model.addAttribute("meetings", meetings);

        Meeting selectedMeeting = null;
        List<MeetingAgendaItem> agendaItems = List.of();
        List<CaseRecord> availableCaseRecords = List.of();

        Map<Long, List<MeetingAgendaDocument>> documentsByAgendaItemId = new HashMap<>();
        Map<Long, List<CaseFile>> availableFilesByAgendaItemId = new HashMap<>();

        if (selectedMeetingId != null) {
            selectedMeeting = meetingService.getMeetingById(selectedMeetingId);
            agendaItems = meetingService.getAgendaItems(selectedMeetingId);

            Long selectedRegistryId = selectedMeeting.getRegistry().getId();
            availableCaseRecords = caseRecordRepository.findByRegistryIdOrderByCreatedAtDesc(selectedRegistryId);
            model.addAttribute("selectedRegistryId", selectedRegistryId);

            for (MeetingAgendaItem agendaItem : agendaItems) {
                documentsByAgendaItemId.put(
                        agendaItem.getId(),
                        meetingService.getAgendaDocuments(agendaItem.getId())
                );

                availableFilesByAgendaItemId.put(
                        agendaItem.getId(),
                        meetingService.getAvailableCaseFilesForAgendaItem(agendaItem.getId())
                );
            }
        }

        model.addAttribute("selectedMeeting", selectedMeeting);
        model.addAttribute("agendaItems", agendaItems);
        model.addAttribute("availableCaseRecords", availableCaseRecords);
        model.addAttribute("documentsByAgendaItemId", documentsByAgendaItemId);
        model.addAttribute("availableFilesByAgendaItemId", availableFilesByAgendaItemId);
    }
}
