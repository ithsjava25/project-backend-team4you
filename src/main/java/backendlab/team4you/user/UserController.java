package backendlab.team4you.user;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
    public String application(){
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
}
