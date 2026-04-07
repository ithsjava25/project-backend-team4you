package backendlab.team4you.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {


    @GetMapping("/profile")
    public String profile() {
        return "fragments/profile :: content";
    }


}
