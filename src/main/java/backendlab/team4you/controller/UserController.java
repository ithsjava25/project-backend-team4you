package backendlab.team4you.controller;


import org.springframework.security.access.prepost.PreAuthorize;
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
        return "index";
    }






     @GetMapping("/admin")
     @PreAuthorize("hasRole('ADMIN')")
    public String admin(){
        return "admin";
    }


}
