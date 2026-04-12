package backendlab.team4you.controller;

import backendlab.team4you.booking.BookingService;
import backendlab.team4you.service.LogService;
import backendlab.team4you.user.UserService;
import groovy.util.logging.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final BookingService bookingService;

    public AdminController(UserService userService, BookingService bookingService) {
        this.userService = userService;
        this.bookingService = bookingService;

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
        return "fragments/admin-users.html :: content";
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

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String admin(){
        return "admin";
    }


    @GetMapping("/admin/applications")
    public String adminApplication(Model model){

        List<String> applications = List.of(
                "Ansökan #1",
                "Ansökan #2",
                "Ansökan #3"
        );

        model.addAttribute("applications", applications);

        return "fragments/admin-applications :: content";
    }

    @PostMapping("/admin/applications")
    public String deleteApplication(@RequestParam String id){


        return "fragments/empty :: content";
    }

    @GetMapping("/admin/bookings")
    public String adminBookings(Model model){

        List<String> bookings = List.of(
                "Bokning #1",
                "Bokning #2",
                "Bokning #3"
        );

        model.addAttribute("bookings", bookings);

        return "fragments/admin-bookings :: content";
    }

    @PostMapping("/admin/bookings")
    public String deleteBooking(@RequestParam String id){

       bookingService.delete(id);

        return "fragments/empty :: content";
    }
}

