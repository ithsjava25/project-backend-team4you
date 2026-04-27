package backendlab.team4you.controller;

import backendlab.team4you.casefile.CaseFile;
import backendlab.team4you.casefile.CaseFileListItemDto;
import backendlab.team4you.casefile.CaseFileService;
import backendlab.team4you.caserecord.CaseRecord;
import backendlab.team4you.caserecord.CaseRecordRepository;
import backendlab.team4you.caserecord.CaseRecordService;
import backendlab.team4you.caserecord.CaseStatus;
import backendlab.team4you.common.ConfidentialityLevel;
import backendlab.team4you.exceptions.CaseRecordNotFoundException;
import backendlab.team4you.exceptions.UserNotFoundException;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserRepository;
import backendlab.team4you.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;

@Controller
@PreAuthorize("hasRole('CASE_OFFICER')")
public class CaseOfficerController {

    private final CaseRecordRepository caseRecordRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final CaseFileService caseFileService;

    public CaseOfficerController(CaseRecordRepository caseRecordRepository, UserRepository userRepository, UserService userService, CaseFileService caseFileService) {
        this.caseRecordRepository = caseRecordRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.caseFileService = caseFileService;
    }

    @GetMapping("/case-officer")
    public String caseOfficerHome() {
        return "case-officer";
    }

    @GetMapping("/case-officer/cases")
    public String listCases(
            @RequestParam(defaultValue = "0") int page,
            @RequestHeader(value = "HX-Request", required = false) String htmxRequest,
            Authentication auth,
            Model model
    ) {
        UserEntity officer = userRepository.findByName(auth.getName())
                .orElseThrow(() -> new UserNotFoundException("Case Officer not found"));

        Page<CaseRecord> cases =
                caseRecordRepository.findByAssignedUserId(officer.getIdAsString(), PageRequest.of(page, 5));

        model.addAttribute("cases", cases.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", cases.getTotalPages());

        if (htmxRequest != null) {
            return "fragments/case-officer-cases :: content";
        }
        return "case-officer-cases";
    }

    @PostMapping("/case-officer/cases/close")
    @ResponseBody
    public String closeCase(@RequestParam String id, Authentication auth) {
        UserEntity officer = userRepository.findByName(auth.getName())
                .orElseThrow(() -> new UserNotFoundException("Case Officer not found"));

        CaseRecord caseRecord = caseRecordRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new CaseRecordNotFoundException(id));

        if (!caseRecord.getAssignedUser().getId().equals(officer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not assigned to this officer");
        }

        caseRecord.setStatus(CaseStatus.CLOSED);
        caseRecordRepository.save(caseRecord);

        return "";
    }
    @GetMapping("/case-officer/cases/{caseRecordId}/files")
    public String getCaseFiles(@PathVariable Long caseRecordId, Principal principal, Model model) {
        UserEntity currentUser = userService.getCurrentUser(principal);
        List<CaseFileListItemDto> files = caseFileService.listFileItemsForViewer(caseRecordId, currentUser);

        model.addAttribute("files", files);
        model.addAttribute("caseRecordId", caseRecordId);

        return "fragments/case-management/case-file-list-officer :: caseFileList";
    }


    @PostMapping("/case-officer/cases/{caseRecordId}/files")
    public String uploadFile(
            @PathVariable Long caseRecordId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("confidentialityLevel") ConfidentialityLevel confidentialityLevel,
            Principal principal,
            Model model
    ) {
        UserEntity currentUser = userService.getCurrentUser(principal);

        try {
            caseFileService.uploadFile(caseRecordId, file, confidentialityLevel, currentUser);
            model.addAttribute("successMessage", "Filen laddades upp");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Kunde inte ladda upp filen: " + e.getMessage());
        }

        return getCaseFiles(caseRecordId, principal, model);
    }

    @GetMapping("/case-officer/cases/{caseRecordId}/files/{fileId}")
    public ResponseEntity<StreamingResponseBody> downloadFile(
            @PathVariable Long caseRecordId,
            @PathVariable Long fileId,
            Principal principal
    ) {
        UserEntity currentUser = userService.getCurrentUser(principal);
        CaseFile caseFile = caseFileService.getCaseFileForViewer(caseRecordId, fileId, currentUser);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (caseFile.getContentType() != null && !caseFile.getContentType().isBlank()) {
            mediaType = MediaType.parseMediaType(caseFile.getContentType());
        }

        StreamingResponseBody body = outputStream -> {
            try (InputStream stream = caseFileService.downloadFile(caseRecordId, fileId, currentUser)) {
                stream.transferTo(outputStream);
            }
        };

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(caseFile.getOriginalFilename(), StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .contentType(mediaType)
                .body(body);
    }

    @DeleteMapping("/case-officer/cases/{caseRecordId}/files/{fileId}")
    public String deleteFile(
            @PathVariable Long caseRecordId,
            @PathVariable Long fileId,
            Principal principal,
            Model model
    ) {

        UserEntity currentUser = userService.getCurrentUser(principal);
        try {
            caseFileService.deleteFile(caseRecordId, fileId, currentUser);
            model.addAttribute("successMessage", "Filen togs bort");
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Kunde inte ta bort filen: " + e.getMessage());
            }
            return getCaseFiles(caseRecordId, principal, model);
    }
}
