package backendlab.team4you.casefile;

import backendlab.team4you.casefile.access.CaseFileAccessService;
import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/cases/{caseRecordId}/files")
public class CaseFileController {

    private final CaseFileService caseFileService;
    private final CaseFileAccessService caseFileAccessService;
    private final UserService userService;

    public CaseFileController(CaseFileService caseFileService,
                              CaseFileAccessService caseFileAccessService,
                              UserService userService) {
        this.caseFileService = caseFileService;
        this.caseFileAccessService = caseFileAccessService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<CaseFileResponseDto> uploadFile(
            @PathVariable Long caseRecordId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("confidentialityLevel") FileConfidentialityLevel confidentialityLevel
    ) throws IOException {
        CaseFile savedFile = caseFileService.uploadFile(caseRecordId, file, confidentialityLevel);
        return ResponseEntity.ok(CaseFileResponseDto.from(savedFile));
    }

    @GetMapping
    public ResponseEntity<List<CaseFileResponseDto>> listFiles(@PathVariable Long caseRecordId) {
        List<CaseFileResponseDto> files = caseFileService.listFiles(caseRecordId).stream()
                .map(CaseFileResponseDto::from)
                .toList();

        return ResponseEntity.ok(files);
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<StreamingResponseBody> downloadFile(
            @PathVariable Long caseRecordId,
            @PathVariable Long fileId,
            Principal principal
    ) {
        UserEntity currentUser = userService.getCurrentUser(principal);
        CaseFile caseFile = caseFileService.getCaseFile(caseRecordId, fileId);

        if (!caseFileAccessService.canViewFile(currentUser, caseFile)) {
            throw new AccessDeniedException("Du har inte behörighet att öppna denna fil.");
        }

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (caseFile.getContentType() != null && !caseFile.getContentType().isBlank()) {
            mediaType = MediaType.parseMediaType(caseFile.getContentType());
        }

        StreamingResponseBody body = outputStream -> {
            try (InputStream stream = caseFileService.downloadFile(caseRecordId, fileId)) {
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

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable Long caseRecordId,
            @PathVariable Long fileId
    ) {
        caseFileService.deleteFile(caseRecordId, fileId);
        return ResponseEntity.noContent().build();
    }
}