package backendlab.team4you.controller;

import backendlab.team4you.application.ApplicationEntity;
import backendlab.team4you.application.ApplicationRepository;
import backendlab.team4you.application.ApplicationService;
import backendlab.team4you.application.ApplicationStatus;
import backendlab.team4you.booking.BookingEntity;
import backendlab.team4you.booking.BookingRepository;
import backendlab.team4you.booking.BookingService;
import backendlab.team4you.log.LogEntity;
import backendlab.team4you.log.LogRepository;
import backendlab.team4you.log.LogService;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserRepository;
import backendlab.team4you.user.UserService;
import groovy.util.logging.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.awt.print.Book;
import java.time.ZoneId;
import java.time.ZonedDateTime;


@Slf4j
@Controller
public class AdminController {

    private final LogService logService;
    private final LogRepository logRepository;

    private final UserService userService;
    private final BookingService bookingService;
    private final ApplicationService applicationService;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public AdminController(LogService logService, LogRepository logRepository, UserService userService, BookingService bookingService, ApplicationService applicationService, ApplicationRepository applicationRepository, UserRepository userRepository, BookingRepository bookingRepository, LogRepository logRepository1) {
        this.logService = logService;
        this.logRepository = logRepository;
        this.userService = userService;
        this.bookingService = bookingService;
        this.applicationService = applicationService;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;



    }

    @GetMapping("/admin/logs")
    public String adminLogs(
            @RequestParam(defaultValue = "0") int page,
            Model model,
            @RequestParam(defaultValue = "displayName") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String search

    ) {

        Page<LogEntity> logs = logRepository.findAll(
                PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        model.addAttribute("logs", logs.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", logs.getTotalPages());

        return "fragments/admin-logs :: content";
    }

    @PostMapping("/admin/bookings/delete")
    public String deleteBooking(
            @RequestParam Long id,
            @RequestParam int page,
            @RequestParam String sort,
            @RequestParam String direction,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        bookingService.deleteBooking(id, userDetails.getUsername());

        return loadBookings(page, sort, direction, model);
    }


    @PostMapping("/admin/users/delete")
    public String deleteUser(
            @RequestParam Long id,
            @RequestParam int page,
            @RequestParam String sort,
            @RequestParam String direction,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        userService.deleteUser(id, userDetails.getUsername());

        return loadUsers(page, sort, direction, model);
    }

    @PostMapping("/admin/logs/delete")
    public String deleteLog(@RequestParam String log) {

        logService.delete(log);

        return "";
    }




    @GetMapping("/admin")
    public String adminDashboard(Model model) {

        model.addAttribute("userCount", userRepository.count());
        model.addAttribute("applicationCount", applicationRepository.count());
        model.addAttribute("bookingCount", bookingRepository.count());

        model.addAttribute("pendingCount",
                applicationRepository.countByStatus(ApplicationStatus.PENDING));



        return "admin";
    }


    @GetMapping("/admin/applications")
    public String adminApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "title") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String search,
            Model model
    ) {

        Sort.Direction dir = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, 5, Sort.by(dir, sort));

        Page<ApplicationEntity> applications;

        if (search != null && !search.isBlank()) {
            applications = applicationRepository
                    .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                            search, search, pageable
                    );
        } else {
            applications = applicationRepository.findAll(pageable);
        }

        model.addAttribute("applications", applications.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", applications.getTotalPages());

        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("search", search);

        return "fragments/admin-applications :: content";
    }


    @GetMapping("/admin/bookings")
    public String adminBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String search,
            Model model
    ) {

        Sort.Direction dir = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, 5, Sort.by(dir, sort));

        Page<BookingEntity> bookings =
                bookingRepository.findAll(PageRequest.of(page, 5, Sort.by(dir, sort)));

        if (search != null && !search.isEmpty()) {
            bookings = bookingRepository.findByStatusContainingIgnoreCaseOrReferenceContainingIgnoreCase(
                    search, search, pageable
            );
        } else {
            bookings = bookingRepository.findAll(pageable);
        }


        model.addAttribute("bookings", bookings.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookings.getTotalPages());

        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("search", search);

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
    public String getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "displayName") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            Model model
    ) {

        Sort.Direction dir = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;




        Page<UserEntity> users = userRepository.findAll(
                PageRequest.of(page, 5, Sort.by(dir, sort))
        );

        model.addAttribute("users", users.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());

        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);

        return "fragments/admin-users :: content";
    }

    @PostMapping("/admin/applications/delete")
    public String deleteApplication(@RequestParam Long id, Model model) {

        applicationService.delete(id);


        model.addAttribute("message", "Ansökan borttagen");

        return "fragments/alert :: success";
    }

    private String loadUsers(int page, String sort, String direction, Model model) {

        Sort.Direction dir = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Page<UserEntity> users = userRepository.findAll(
                PageRequest.of(page, 5, Sort.by(dir, sort))
        );

        model.addAttribute("users", users.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);

        return "fragments/admin-users :: content";
    }

    private String loadBookings(int page, String sort, String direction, Model model) {

        Sort.Direction dir = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Page<BookingEntity> bookings = bookingRepository.findAll(
                PageRequest.of(page, 5, Sort.by(dir, sort))
        );

        model.addAttribute("bookings", bookings.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookings.getTotalPages());
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);

        return "fragments/admin-bookings :: content";
    }
    private Sort.Direction getDirection(String direction) {
        return direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
    }

    @GetMapping("/admin/applications/{id}")
    public String getApplication(@PathVariable Long id, Model model) {

        ApplicationEntity application = applicationRepository.findById(id)
                .orElseThrow();

        model.addAttribute("application", application);

        return "admin/application-detail";
    }

    @GetMapping("/admin/bookings/{id}")
    public String getBooking(@PathVariable Long id, Model model) {
        BookingEntity booking = bookingRepository.findById(id).orElseThrow();
        model.addAttribute("booking", booking);
        return "admin/booking-detail";
    }

    @GetMapping("/admin/content")
    public String adminContent(Model model) {

        model.addAttribute("userCount", userRepository.count());
        model.addAttribute("applicationCount", applicationRepository.count());
        model.addAttribute("bookingCount", bookingRepository.count());

        return "admin :: content";
    }

}


