package backendlab.team4you.protocol;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/protocols")
@PreAuthorize("hasRole('ADMIN')")
public class ProtocolPdfController {

    private final ProtocolPdfService protocolPdfService;

    public ProtocolPdfController(
            ProtocolPdfService protocolPdfService) {
        this.protocolPdfService = protocolPdfService;
    }

    @GetMapping("/{protocolId}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long protocolId) {
        byte[] pdf = protocolPdfService.generateFullPdf(protocolId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=protocol-" + protocolId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
