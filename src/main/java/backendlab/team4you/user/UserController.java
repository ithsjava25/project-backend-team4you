package backendlab.team4you.user;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
public class UserController {



    @GetMapping("/")
    public String homepage(){
        return "home";
    }


    @GetMapping("/booking")
    public String booking(){
        return "booking";
    }

    @GetMapping("/application")
    public String application(Model model){
        List<String> application = Arrays.asList("Ärende #1 - Pågående", "Ärende #15 - Väntar på beslut");
        model.addAttribute("application", application);
        model.addAttribute("userName", "Test user");

        return "application";
    }

    @GetMapping("/profile")
    public String profile(){
        return "profile";
    }

    @GetMapping("/admin")
    public String admin(){
        return "admin";
    }
    @GetMapping("/contact")
    public String contact(){
        return "contact";
    }
}
