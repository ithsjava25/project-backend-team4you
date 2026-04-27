package backendlab.team4you.controller;

import backendlab.team4you.application.ApplicationEntity;
import backendlab.team4you.application.ApplicationRepository;
import backendlab.team4you.application.ApplicationService;
import backendlab.team4you.audit.AuditAction;
import backendlab.team4you.audit.AuditLog;
import backendlab.team4you.audit.AuditLogRepository;
import backendlab.team4you.booking.BookingService;
import backendlab.team4you.caserecord.CaseRecord;
import backendlab.team4you.caserecord.CaseRecordRepository;
import backendlab.team4you.exceptions.CaseRecordNotFoundException;
import backendlab.team4you.exceptions.UserNotFoundException;
import backendlab.team4you.service.LogService;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserRepository;
import backendlab.team4you.user.UserRole;
import backendlab.team4you.user.UserService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static backendlab.team4you.user.UserRole.CASE_OFFICER;



@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final LogService logService = new LogService();
    private final List<String> logs = logService.getLogs();
    private final AuditLogRepository auditLogRepository;

    private final UserService userService;
    private final BookingService bookingService;
    private final ApplicationService applicationService;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final CaseRecordRepository caseRecordRepository;

    public AdminController(AuditLogRepository auditLogRepository, UserService userService,
                           BookingService bookingService,
                           ApplicationService applicationService,
                           ApplicationRepository applicationRepository,
                           UserRepository userRepository,
                           CaseRecordRepository caseRecordRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userService = userService;
        this.bookingService = bookingService;
        this.applicationService = applicationService;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.caseRecordRepository = caseRecordRepository;
    }



    @PostMapping("/admin/users")
    @AuditAction(action = "DELETE_USER", entity = "USER")
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
    public String admin(Authentication auth) {
        System.out.println(auth.getAuthorities());

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
    @AuditAction(action = "DELETE_USER", entity = "USER")
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


    @GetMapping("/admin/logs")
    public String viewLogs(Model model, @RequestHeader(value = "HX-Request", required = false) String htmx) {

        var pageable = PageRequest.of(0, 50, Sort.by("timestamp").descending());

        Page<AuditLog> logsPage = auditLogRepository.findAll(pageable);

        model.addAttribute("logs", logsPage.getContent());

        if (htmx != null) {
            return "fragments/admin-logs :: content";
        }
        return "fragments/admin-logs";
    }


    @GetMapping("/admin/cases")
    public String listCases(
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Page<CaseRecord> cases = caseRecordRepository.findAll(PageRequest.of(page, 10));
        List<UserEntity> officers = userRepository.findByRole(UserRole.CASE_OFFICER);

        model.addAttribute("cases", cases.getContent());
        model.addAttribute("officers", officers);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", cases.getTotalPages());

        return "fragments/admin-cases :: content";
    }

    @PostMapping("/admin/cases/assign")
    public String assignCase(
            @RequestParam Long caseId,
            @RequestParam String officerId,
            Model model
    ) {
        CaseRecord caseRecord = caseRecordRepository.findById(caseId)
                .orElseThrow(() -> new CaseRecordNotFoundException(caseId));

        UserEntity officer = userRepository.findById(officerId)
                .orElseThrow(() -> new UserNotFoundException("Officer not found"));

        if (officer.getRole() != UserRole.CASE_OFFICER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected user is not a case officer");
        }

        caseRecord.setAssignedUser(officer);
        caseRecordRepository.save(caseRecord);

        model.addAttribute("message", "Ärende tilldelat till " + officer.getDisplayName());
        return "fragments/alert :: success";
    }

}
