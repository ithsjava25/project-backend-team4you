package backendlab.team4you.controller;

import backendlab.team4you.contact.ContactRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContactController.class)
class ContactControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ContactRepository contactRepository;

    @Test
    void contact_shouldReturnContactPage() throws Exception {
        mockMvc.perform(get("/contact"))
                .andExpect(status().isOk())
                .andExpect(view().name("contact"));
    }

    @Test
    void receiveMessage_shouldSaveAndReturnSuccess() throws Exception {
        mockMvc.perform(post("/contact/send")
                        .param("firstName", "Sven")
                        .param("lastName", "Svensson")
                        .param("email", "sven@test.com")
                        .param("phone", "0701234567")
                        .param("message", "Hello i am Sven"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/contact-fragments :: success-message"));

        verify(contactRepository).save(any());
    }

    @Test
    void receiveMessage_shouldReturnForm_whenValidationFails() throws Exception {
        mockMvc.perform(post("/contact/send")
                        .param("firstName", "")
                        .param("lastName", "")
                        .param("email", "")
                        .param("phone", "")
                        .param("message", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/contact-fragments :: contact-form"));
    }
}
