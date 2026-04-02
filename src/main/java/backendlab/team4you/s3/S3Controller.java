package backendlab.team4you.s3;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        String key = file.getOriginalFilename();
        s3Service.uploadFile(key, file.getBytes(), file.getContentType());
        return ResponseEntity.ok("File uploaded: " + key);
    }

    // GET /api/files/download/{key} — download a file
    @GetMapping("/download/{key}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String key) throws IOException {
        InputStream stream = s3Service.downloadFile(key);
        byte[] bytes = stream.readAllBytes();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + key + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }

    // DELETE /api/files/delete/{key} — delete a file
    @DeleteMapping("/delete/{key}")
    public ResponseEntity<String> deleteFile(@PathVariable String key) {
        s3Service.deleteFile(key);
        return ResponseEntity.ok("File deleted: " + key);
    }
}