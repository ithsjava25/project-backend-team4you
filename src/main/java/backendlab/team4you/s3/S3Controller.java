package backendlab.team4you.s3;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import backendlab.team4you.exceptions.FileStorageConfigurationException;


import java.io.IOException;
import java.io.InputStream;

// Handles HTTP requests and returns data
@RestController
// All endpoints start with /api/files
@RequestMapping("/api/files")
public class S3Controller {

    private final S3Service s3Service;

    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    // POST /api/files/upload — upload a file
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        String key = file.getOriginalFilename();
        if (key == null || key.isBlank()) {
            return ResponseEntity.badRequest().body("Invalid filename");
        }
        try {
            s3Service.uploadFile(key, file.getBytes(), file.getContentType());
            return ResponseEntity.ok("File uploaded: " + key);
        } catch (FileStorageConfigurationException e) {
            return ResponseEntity.internalServerError().body("Kunde inte ladda upp filen: " + key);
        }
    }

    // GET /api/files/download/{key} — download a file
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/download/{key}")
    public ResponseEntity<?> downloadFile(@PathVariable String key) throws IOException {
        try (InputStream stream = s3Service.downloadFile(key)) {
            byte[] bytes = stream.readAllBytes();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + key + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(bytes);
        } catch (FileStorageConfigurationException e) {
            return ResponseEntity.internalServerError().body("Kunde inte ladda ner filen: " + key);
        }
    }

    // DELETE /api/files/delete/{key} — delete a file
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{key}")
    public ResponseEntity<String> deleteFile(@PathVariable String key) {
        try {
            s3Service.deleteFile(key);
            return ResponseEntity.ok("File deleted: " + key);
        } catch (FileStorageConfigurationException e) {
            return ResponseEntity.internalServerError().body("Kunde inte radera filen: " + key);
        }
    }
}
