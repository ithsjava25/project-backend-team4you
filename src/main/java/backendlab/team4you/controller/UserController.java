package backendlab.team4you.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {



    @GetMapping("/")
    public String homepage(){
        return "login";
    }

    @GetMapping("/home")
    public String userHome() {
        return "home";
    }
}
