package backendlab.team4you.controller;

import backendlab.team4you.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final UserService userService;

    public DashboardController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(){
        return "dashboard";
    }

    @GetMapping("/dashboard/home")
    public String dashboardHome() {
        return "dashboard :: content";
    }
}
