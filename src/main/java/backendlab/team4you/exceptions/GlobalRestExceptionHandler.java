package backendlab.team4you.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.IOException;
import java.time.LocalDateTime;

@RestControllerAdvice(basePackages = {
        "backendlab.team4you.casefile",
        "backendlab.team4you.caserecord",
        "backendlab.team4you.registry",
        "backendlab.team4you.user",
        "backendlab.team4you.controller",
        "backendlab.team4you.protocol"
})
public class GlobalRestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalRestExceptionHandler.class);

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponseDto(
                        HttpStatus.FORBIDDEN.value(),
                        "forbidden",
                        ex.getMessage(),
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler({
            CaseFileNotFoundException.class,
            CaseRecordNotFoundException.class,
            RegistryNotFoundException.class,
            UserNotFoundException.class,
            MeetingNotFoundException.class,
            MeetingAgendaItemNotFoundException.class,
            MeetingAgendaDocumentNotFoundException.class,
            ProtocolNotFoundException.class
    })
    public ResponseEntity<ErrorResponseDto> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDto(
                        HttpStatus.NOT_FOUND.value(),
                        "not found",
                        ex.getMessage(),
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler({
            InvalidFileNameException.class,
            IllegalArgumentException.class,
            IllegalStateException.class,
            InvalidMeetingStateException.class
    })
    public ResponseEntity<ErrorResponseDto> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(
                        HttpStatus.BAD_REQUEST.value(),
                        "bad request",
                        ex.getMessage(),
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(FileTooLargeException.class)
    public ResponseEntity<ErrorResponseDto> handleFileTooLarge(FileTooLargeException ex) {
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE)
                .body(new ErrorResponseDto(
                        HttpStatus.CONTENT_TOO_LARGE.value(),
                        "content too large",
                        ex.getMessage(),
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler({
            DuplicateRegistryNameException.class,
            DuplicateRegistryCodeException.class,
            DuplicateEmailException.class,
            FileKeyConflictException.class,
            DuplicateMeetingAgendaItemException.class,
            DuplicateMeetingAgendaDocumentException.class,
            ProtocolAlreadyExistsException.class
    })
    public ResponseEntity<ErrorResponseDto> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDto(
                        HttpStatus.CONFLICT.value(),
                        "conflict",
                        ex.getMessage(),
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(
                        HttpStatus.BAD_REQUEST.value(),
                        "bad request",
                        "Ogiltiga indata.",
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(
                        HttpStatus.BAD_REQUEST.value(),
                        "bad request",
                        "Begäran kunde inte tolkas.",
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDto> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(
                        HttpStatus.BAD_REQUEST.value(),
                        "bad request",
                        "Obligatorisk parameter saknas: " + ex.getParameterName(),
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Ogiltigt värde för parameter: " + ex.getName();

        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            message = "Ogiltigt värde för parameter '" + ex.getName() + "'.";
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(
                        HttpStatus.BAD_REQUEST.value(),
                        "bad request",
                        message,
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ErrorResponseDto(
                        HttpStatus.METHOD_NOT_ALLOWED.value(),
                        "method not allowed",
                        "HTTP-metoden stöds inte för denna endpoint.",
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(FileStorageConfigurationException.class)
    public ResponseEntity<ErrorResponseDto> handleFileStorageConfiguration(FileStorageConfigurationException ex) {
        log.error("File storage configuration error", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "internal server error",
                        "Filhanteringen är tillfälligt otillgänglig.",
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler({
            IOException.class,
            DataIntegrityViolationException.class
    })
    public ResponseEntity<ErrorResponseDto> handleTechnicalExceptions(Exception ex) {
        log.error("Technical error in REST flow", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "internal server error",
                        "Ett internt fel uppstod.",
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleUnexpected(Exception ex) {
        log.error("Unexpected REST error", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "internal server error",
                        "Ett oväntat fel uppstod.",
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(FileInUseException.class)
    public ResponseEntity<ErrorResponseDto> handleFileInUse(FileInUseException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDto(
                        HttpStatus.CONFLICT.value(),
                        "conflict",
                        ex.getMessage(),
                        LocalDateTime.now()
                ));
    }
}
