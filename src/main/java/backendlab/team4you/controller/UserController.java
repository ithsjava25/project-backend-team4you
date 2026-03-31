package backendlab.team4you.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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


    @GetMapping("/profile")
    public String profile(){
        return "profile";
    }

    @GetMapping("/admin")
    public String admin(){
        return "admin";
    }
}
