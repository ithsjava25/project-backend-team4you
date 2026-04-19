package backendlab.team4you;


import backendlab.team4you.controller.UserController;
import backendlab.team4you.exceptions.UserNotFoundException;
import backendlab.team4you.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;


    @Test
    void get404WhenBookNotFound() throws Exception {
        when(userService.findById(999L))
                .thenThrow(new UserNotFoundException("Not found"));


        mockMvc.perform(get("/user/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void userShouldReturnUpdateView() throws Exception {

        mockMvc.perform(get("/user/update/1"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/edit/1"));
    }
}
