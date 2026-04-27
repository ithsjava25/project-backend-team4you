package backendlab.team4you.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice(basePackages = {
        "backendlab.team4you.casefile.ui",
        "backendlab.team4you.meeting",
        "backendlab.team4you.protocol"
})
public class GlobalViewExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalViewExceptionHandler.class);

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleUserNotFound(UserNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(DuplicateEmailException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicateEmail(DuplicateEmailException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    @ExceptionHandler({
            CaseRecordNotFoundException.class,
            RegistryNotFoundException.class,
            CaseFileNotFoundException.class,
            MeetingNotFoundException.class,
            MeetingAgendaDocumentNotFoundException.class,
            ProtocolNotFoundException.class,
            ProtocolParagraphNotFoundException.class,
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(RuntimeException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(AccessDeniedException ex, Model model) {
        model.addAttribute("errorMessage", "Du har inte behörighet att utföra den här åtgärden.");
        return "error";
    }

    @ExceptionHandler(FileStorageConfigurationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleStorageConfiguration(FileStorageConfigurationException ex, Model model) {
        log.error("File storage configuration error in view flow", ex);
        model.addAttribute("errorMessage", "Filhanteringen är tillfälligt otillgänglig.");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleUnexpected(Exception ex, Model model) {
        log.error("Unexpected view error", ex);
        model.addAttribute("errorMessage", "Något gick fel. Försök igen.");
        return "error";
    }

    @ExceptionHandler(ProtocolAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleProtocolAlreadyExists(ProtocolAlreadyExistsException ex, Model model) {
        model.addAttribute("errorMessage", "Ett protokoll finns redan för det här sammanträdet.");
        return "error";
    }
}
