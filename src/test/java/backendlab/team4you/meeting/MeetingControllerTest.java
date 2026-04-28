package backendlab.team4you.meeting;

import backendlab.team4you.caserecord.CaseRecord;
import backendlab.team4you.caserecord.CaseRecordRepository;
import backendlab.team4you.exceptions.InvalidMeetingStateException;
import backendlab.team4you.registry.Registry;
import backendlab.team4you.registry.RegistryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MeetingController.class)
class MeetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MeetingService meetingService;

    @MockitoBean
    private RegistryRepository registryRepository;

    @MockitoBean
    private CaseRecordRepository caseRecordRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /admin/meetings should return meetings fragment")
    void meetingsPage_shouldReturnMeetingsFragment() throws Exception {
        Registry registry = registry(1L, "Kommunstyrelsen", "KS");

        when(registryRepository.findAll()).thenReturn(List.of(registry));
        when(meetingService.getAllMeetings()).thenReturn(List.of());

        mockMvc.perform(get("/admin/meetings")
                        .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/admin-meetings :: content"))
                .andExpect(model().attributeExists("registries"))
                .andExpect(model().attributeExists("meetings"))
                .andExpect(model().attribute("selectedMeeting", nullValue()))
                .andExpect(model().attributeExists("agendaItems"))
                .andExpect(model().attributeExists("availableCaseRecords"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /admin/meetings should create meeting and return success message")
    void createMeeting_shouldReturnFragmentAndSuccessMessage_whenRequestIsValid() throws Exception {
        Registry registry = registry(1L, "Kommunstyrelsen", "KS");
        Meeting createdMeeting = meeting(10L, registry, "KS april");

        when(registryRepository.findAll()).thenReturn(List.of(registry));
        when(meetingService.createMeeting(
                eq(1L),
                eq("KS april"),
                eq(LocalDateTime.parse("2026-05-10T13:00")),
                eq(LocalDateTime.parse("2026-05-10T15:00")),
                eq("Sessionssalen"),
                eq("Anteckning")
        )).thenReturn(createdMeeting);

        when(meetingService.getMeetingsForRegistry(1L)).thenReturn(List.of(createdMeeting));
        when(meetingService.getMeetingById(10L)).thenReturn(createdMeeting);
        when(meetingService.getAgendaItems(10L)).thenReturn(List.of());
        when(caseRecordRepository.findByRegistryIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        mockMvc.perform(post("/admin/meetings")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("registryId", "1")
                        .param("title", "KS april")
                        .param("startsAt", "2026-05-10T13:00")
                        .param("endsAt", "2026-05-10T15:00")
                        .param("location", "Sessionssalen")
                        .param("notes", "Anteckning"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/admin-meetings :: content"))
                .andExpect(model().attribute("successMessage", "Sammanträdet skapades."));

        verify(meetingService).createMeeting(
                1L,
                "KS april",
                LocalDateTime.parse("2026-05-10T13:00"),
                LocalDateTime.parse("2026-05-10T15:00"),
                "Sessionssalen",
                "Anteckning"
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /admin/meetings should return error message when service throws")
    void createMeeting_shouldReturnFragmentAndErrorMessage_whenServiceThrows() throws Exception {
        Registry registry = registry(1L, "Kommunstyrelsen", "KS");

        when(registryRepository.findAll()).thenReturn(List.of(registry));
        when(meetingService.createMeeting(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        )).thenThrow(new InvalidMeetingStateException("Titel måste anges."));
        when(registryRepository.existsById(1L)).thenReturn(true);
        when(meetingService.getMeetingsForRegistry(1L)).thenReturn(List.of());

        mockMvc.perform(post("/admin/meetings")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("registryId", "1")
                        .param("title", " ")
                        .param("startsAt", "2026-05-10T13:00"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/admin-meetings :: content"))
                .andExpect(model().attribute("errorMessage", "Titel måste anges."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /admin/meetings/{meetingId} should show selected meeting")
    void showMeeting_shouldReturnFragmentWithSelectedMeeting() throws Exception {
        Registry registry = registry(1L, "Kommunstyrelsen", "KS");
        Meeting meeting = meeting(10L, registry, "KS april");

        when(registryRepository.findAll()).thenReturn(List.of(registry));
        when(meetingService.getMeetingById(10L)).thenReturn(meeting);
        when(meetingService.getMeetingsForRegistry(1L)).thenReturn(List.of(meeting));
        when(meetingService.getAgendaItems(10L)).thenReturn(List.of());
        when(caseRecordRepository.findByRegistryIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        mockMvc.perform(get("/admin/meetings/10")
                        .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/admin-meetings :: content"))
                .andExpect(model().attribute("selectedMeeting", meeting));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /admin/meetings/{meetingId}/agenda-items should add agenda item")
    void addAgendaItem_shouldReturnFragmentAndSuccessMessage() throws Exception {
        Registry registry = registry(1L, "Kommunstyrelsen", "KS");
        Meeting meeting = meeting(10L, registry, "KS april");
        CaseRecord caseRecord = mock(CaseRecord.class);

        when(registryRepository.findAll()).thenReturn(List.of(registry));
        when(meetingService.getMeetingById(10L)).thenReturn(meeting);
        when(meetingService.getMeetingsForRegistry(1L)).thenReturn(List.of(meeting));
        when(meetingService.getAgendaItems(10L)).thenReturn(List.of());
        when(caseRecordRepository.findByRegistryIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(caseRecord));

        mockMvc.perform(post("/admin/meetings/10/agenda-items")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("caseRecordId", "100"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/admin-meetings :: content"))
                .andExpect(model().attribute("successMessage", "Ärendet lades till på sammanträdet."));

        verify(meetingService).addCaseRecordToMeeting(10L, 100L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /admin/meetings/{meetingId}/update should update meeting")
    void updateMeeting_shouldReturnFragmentAndSuccessMessage() throws Exception {
        Registry registry = registry(1L, "Kommunstyrelsen", "KS");
        Meeting meeting = meeting(10L, registry, "KS april");
        Meeting updatedMeeting = meeting(10L, registry, "KS april uppdaterad");

        when(registryRepository.findAll()).thenReturn(List.of(registry));
        when(meetingService.updateMeeting(
                eq(10L),
                eq("KS april uppdaterad"),
                eq(LocalDateTime.parse("2026-05-10T13:00")),
                eq(LocalDateTime.parse("2026-05-10T15:00")),
                eq("Sessionssalen"),
                eq("Nya anteckningar"),
                eq(MeetingStatus.PREPARING)
        )).thenReturn(updatedMeeting);

        when(meetingService.getMeetingsForRegistry(1L)).thenReturn(List.of(updatedMeeting));
        when(meetingService.getMeetingById(10L)).thenReturn(updatedMeeting);
        when(meetingService.getAgendaItems(10L)).thenReturn(List.of());
        when(caseRecordRepository.findByRegistryIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        mockMvc.perform(post("/admin/meetings/10/update")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("title", "KS april uppdaterad")
                        .param("startsAt", "2026-05-10T13:00")
                        .param("endsAt", "2026-05-10T15:00")
                        .param("location", "Sessionssalen")
                        .param("notes", "Nya anteckningar")
                        .param("status", "PREPARING"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/admin-meetings :: content"))
                .andExpect(model().attribute("successMessage", "Sammanträdet uppdaterades."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /admin/meetings/{meetingId}/delete should delete meeting")
    void deleteMeeting_shouldReturnFragmentAndSuccessMessage() throws Exception {
        Registry registry = registry(1L, "Kommunstyrelsen", "KS");
        Meeting meeting = meeting(10L, registry, "KS april");

        when(registryRepository.findAll()).thenReturn(List.of(registry));
        when(meetingService.getMeetingById(10L)).thenReturn(meeting);
        when(meetingService.getMeetingsForRegistry(1L)).thenReturn(List.of());

        mockMvc.perform(post("/admin/meetings/10/delete")
                        .with(csrf())
                        .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/admin-meetings :: content"))
                .andExpect(model().attribute("successMessage", "Sammanträdet togs bort."));

        verify(meetingService).deleteMeeting(10L);
    }

    private Registry registry(Long id, String name, String code) {
        Registry registry = new Registry(name, code);
        setField(registry, "id", id);
        return registry;
    }

    private Meeting meeting(Long id, Registry registry, String title) {
        Meeting meeting = new Meeting(
                registry,
                title,
                LocalDateTime.of(2026, 5, 10, 13, 0),
                LocalDateTime.of(2026, 5, 10, 15, 0),
                "Sessionssalen",
                MeetingStatus.PLANNED,
                "Anteckning"
        );
        setField(meeting, "id", id);
        return meeting;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
