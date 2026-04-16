package backendlab.team4you.controller;

import backendlab.team4you.service.LogService;
import backendlab.team4you.user.UserService;
import groovy.util.logging.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Slf4j
@Controller
public class AdminController {

    private final LogService logService = new LogService();

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String admin(){
        return "dashboard";
    }

    @GetMapping("/admin/logs")
    public String logs(Model model) {
        List<String> logs = List.of(
                "User johndoe loggade in",
                "Ny användare registrerad",
                "Admin tog bort user #123"
        );

        model.addAttribute("logs", logs);
        return "fragments/admin-logs :: content";
    }


    @GetMapping("/admin/users")
    public String adminLogs(Model model){
        List<String> users = List.of(
                "User johndoe",
                "User janedoe",
                "User anna"
        );

        model.addAttribute("users", users);
        return "fragments/admin-users :: content";
    }

    @PostMapping("/admin/users")
    public String deleteUser(@RequestParam String id){
        userService.deleteUser(id);
        return "";
    }

    @PostMapping("/admin/logs/delete")
    public String deleteLog(@RequestParam String log) {
        logService.delete(log);
        return "";
    }



}
