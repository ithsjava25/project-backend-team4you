package backendlab.team4you.casefile;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/cases/{caseRecordId}/files")
public class CaseFileController {

    private final CaseFileService caseFileService;

    public CaseFileController(CaseFileService caseFileService) {
        this.caseFileService = caseFileService;
    }

    @PostMapping
    public ResponseEntity<CaseFileResponseDto> uploadFile(
            @PathVariable Long caseRecordId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        CaseFile savedFile = caseFileService.uploadFile(caseRecordId, file);
        return ResponseEntity.ok(CaseFileResponseDto.from(savedFile));
    }

    @GetMapping
    public ResponseEntity <List<CaseFileResponseDto>> listFiles(@PathVariable Long caseRecordId) {
        List<CaseFileResponseDto> files = caseFileService.listFiles(caseRecordId).stream()
                .map(CaseFileResponseDto::from)
                .toList();

        return ResponseEntity.ok(files);
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable Long caseRecordId,
            @PathVariable Long fileId
    ) throws IOException {
        CaseFile caseFile = caseFileService.getCaseFile(caseRecordId, fileId);

        try (InputStream stream = caseFileService.downloadFile(caseRecordId, fileId)) {
            byte[] bytes = stream.readAllBytes();

            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
            if (caseFile.getContentType() != null && !caseFile.getContentType().isBlank()) {
                mediaType = MediaType.parseMediaType(caseFile.getContentType());
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + caseFile.getOriginalFilename() + "\"")
                    .contentType(mediaType)
                    .body(bytes);
        }
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
