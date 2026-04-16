package backendlab.team4you.controller;

import backendlab.team4you.application.ApplicationEntity;
import backendlab.team4you.application.ApplicationRepository;
import backendlab.team4you.application.ApplicationService;
import backendlab.team4you.booking.BookingService;
import backendlab.team4you.service.LogService;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserService;
import groovy.util.logging.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Scanner;


@Slf4j
@Controller
public class AdminController {

    private final LogService logService = new LogService();

    private final UserService userService;
    private final BookingService bookingService;
    private final ApplicationService applicationService;
    private final ApplicationRepository applicationRepository;

    public AdminController(UserService userService, BookingService bookingService, ApplicationService applicationService, ApplicationRepository applicationRepository) {
        this.userService = userService;
        this.bookingService = bookingService;
        this.applicationService = applicationService;
        this.applicationRepository = applicationRepository;


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




    @PostMapping("/admin/users")
    public String deleteUser(@RequestParam String id, Model model){

        userService.deleteUser(id);

        model.addAttribute("message", "Användare borttagen");

        return "fragments/alert :: success";
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
    public String adminApplications(
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {


        Page<ApplicationEntity> applications =
                applicationRepository.findAll(PageRequest.of(page, 5));

        model.addAttribute("applications", applications.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", applications.getTotalPages());

        return "fragments/admin-applications :: content";
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
    public ResponseEntity<Void> deleteBooking(@RequestParam Long id){
        try {
            bookingService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found", ex);
        }
    }

    @GetMapping("/admin/users")
    public String getUsers(@RequestParam(defaultValue = "0") int page,
                           Model model) {

        Page<UserEntity> users = userService.getUsers(page, 5);

        model.addAttribute("users", users.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());

        return "fragments/admin-users :: content";
    }

    @PostMapping("/admin/applications/delete")
    public String deleteApplication(@RequestParam Long id, Model model) {

        applicationService.delete(id);

        model.addAttribute("message", "Ansökan borttagen");

        return "fragments/alert :: success";
    }
}


