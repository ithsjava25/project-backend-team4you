package backendlab.team4you.protocol;

import backendlab.team4you.meeting.Meeting;
import backendlab.team4you.meeting.MeetingRepository;
import backendlab.team4you.meeting.MeetingStatus;
import backendlab.team4you.registry.Registry;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserRole;
import backendlab.team4you.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProtocolController.class)
class ProtocolControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProtocolService protocolService;

    @MockitoBean
    private ProtocolRepository protocolRepository;

    @MockitoBean
    private MeetingRepository meetingRepository;

    @MockitoBean
    private ProtocolViewService protocolViewService;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /admin/protocols should return protocols fragment")
    void protocolsPage_shouldReturnProtocolsFragment() throws Exception {
        Registry registry = registry(1L, "Kommunstyrelsen", "KS");
        Meeting meeting = meeting(10L, registry, "KS april");
        Protocol protocol = protocol(100L, meeting, registry);

        when(meetingRepository.findCompletedMeetingsWithoutProtocol()).thenReturn(List.of(meeting));
        when(protocolRepository.findAll()).thenReturn(List.of(protocol));

        mockMvc.perform(get("/admin/protocols")
                        .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/admin-protocols :: content"))
                .andExpect(model().attributeExists("completedMeetingsWithoutProtocol"))
                .andExpect(model().attributeExists("protocols"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /admin/protocols/meetings/{meetingId} should create protocol")
    void createProtocol_shouldReturnFragmentAndSuccessMessage() throws Exception {
        Registry registry = registry(1L, "Kommunstyrelsen", "KS");
        Meeting meeting = meeting(10L, registry, "KS april");
        Protocol protocol = protocol(100L, meeting, registry);
        UserEntity currentUser = adminUser();

        when(userService.getCurrentUser(any())).thenReturn(currentUser);
        when(protocolViewService.getParagraphsForViewer(100L, currentUser)).thenReturn(List.of());

        when(protocolService.createProtocolForCompletedMeeting(10L)).thenReturn(protocol);
        when(meetingRepository.findCompletedMeetingsWithoutProtocol()).thenReturn(List.of());
        when(protocolRepository.findAll()).thenReturn(List.of(protocol));

        mockMvc.perform(post("/admin/protocols/meetings/10")
                        .with(csrf())
                        .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/admin-protocols :: content"))
                .andExpect(model().attribute("successMessage", "Protokoll skapades."))
                .andExpect(model().attribute("selectedProtocol", protocol))
                .andExpect(model().attributeExists("paragraphViews"));

        verify(protocolService).createProtocolForCompletedMeeting(10L);
        verify(protocolViewService).getParagraphsForViewer(100L, currentUser);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /admin/protocols/{protocolId} should show selected protocol")
    void viewProtocol_shouldReturnFragmentWithSelectedProtocol() throws Exception {
        Registry registry = registry(1L, "Kommunstyrelsen", "KS");
        Meeting meeting = meeting(10L, registry, "KS april");
        Protocol protocol = protocol(100L, meeting, registry);
        UserEntity currentUser = adminUser();

        when(userService.getCurrentUser(any())).thenReturn(currentUser);
        when(protocolViewService.getParagraphsForViewer(100L, currentUser)).thenReturn(List.of());

        when(protocolService.getProtocol(100L)).thenReturn(protocol);
        when(meetingRepository.findCompletedMeetingsWithoutProtocol()).thenReturn(List.of());
        when(protocolRepository.findAll()).thenReturn(List.of(protocol));

        mockMvc.perform(get("/admin/protocols/100")
                        .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/admin-protocols :: content"))
                .andExpect(model().attribute("selectedProtocol", protocol))
                .andExpect(model().attributeExists("paragraphViews"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /admin/protocols/paragraphs/{paragraphId}/decision should update paragraph decision")
    void updateParagraphDecision_shouldReturnFragmentAndSuccessMessage() throws Exception {
        Registry registry = registry(1L, "Kommunstyrelsen", "KS");
        Meeting meeting = meeting(10L, registry, "KS april");
        Protocol protocol = protocol(100L, meeting, registry);
        UserEntity currentUser = adminUser();

        when(userService.getCurrentUser(any())).thenReturn(currentUser);
        when(protocolViewService.getParagraphsForViewer(100L, currentUser)).thenReturn(List.of());

        when(protocolService.updateParagraphDecision(
                200L,
                ProtocolDecisionType.REJECTED,
                "Kommunstyrelsen beslutar att avslå ärendet."
        )).thenReturn(protocol);

        when(meetingRepository.findCompletedMeetingsWithoutProtocol()).thenReturn(List.of());
        when(protocolRepository.findAll()).thenReturn(List.of(protocol));

        mockMvc.perform(post("/admin/protocols/paragraphs/200/decision")
                        .with(csrf())
                        .header("HX-Request", "true")
                        .param("decisionType", "REJECTED")
                        .param("decisionText", "Kommunstyrelsen beslutar att avslå ärendet."))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/admin-protocols :: content"))
                .andExpect(model().attribute("successMessage", "Beslut sparades."))
                .andExpect(model().attribute("selectedProtocol", protocol))
                .andExpect(model().attributeExists("paragraphViews"));

        verify(protocolService).updateParagraphDecision(
                200L,
                ProtocolDecisionType.REJECTED,
                "Kommunstyrelsen beslutar att avslå ärendet."
        );
        verify(protocolViewService).getParagraphsForViewer(100L, currentUser);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /admin/protocols/paragraphs/{paragraphId}/decision-text should return decision text fragment")
    void getDefaultDecisionText_shouldReturnDecisionTextFragment() throws Exception {
        when(protocolService.buildDefaultDecisionText(200L, ProtocolDecisionType.REJECTED))
                .thenReturn("Kommunstyrelsen beslutar att avslå ärendet.");

        mockMvc.perform(get("/admin/protocols/paragraphs/200/decision-text")
                        .header("HX-Request", "true")
                        .param("decisionType", "REJECTED"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/admin-protocols :: decisionTextField"))
                .andExpect(model().attribute("paragraphId", 200L))
                .andExpect(model().attribute(
                        "decisionText",
                        "Kommunstyrelsen beslutar att avslå ärendet."
                ));
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
                MeetingStatus.COMPLETED,
                "Anteckning"
        );
        setField(meeting, "id", id);
        return meeting;
    }

    private Protocol protocol(Long id, Meeting meeting, Registry registry) {
        Protocol protocol = new Protocol(
                meeting,
                registry,
                "Protokoll - " + registry.getName() + " - 2026",
                2026
        );
        setField(protocol, "id", id);
        return protocol;
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

    private UserEntity adminUser() {
        UserEntity user = new UserEntity();
        user.setName("admin");
        user.setRole(UserRole.ADMIN);
        return user;
    }
}
