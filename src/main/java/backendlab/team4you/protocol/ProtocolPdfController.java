package backendlab.team4you.protocol;

import backendlab.team4you.user.UserEntity;
import backendlab.team4you.user.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/admin/protocols")
@PreAuthorize("hasRole('ADMIN')")
public class ProtocolPdfController {

    private final ProtocolPdfService protocolPdfService;
    private final UserService userService;

    public ProtocolPdfController(
            ProtocolPdfService protocolPdfService,
            UserService userService) {
        this.protocolPdfService = protocolPdfService;
        this.userService = userService;
    }

    @GetMapping("/{protocolId}/pdf")
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable Long protocolId,
            Principal principal
    ) {
        UserEntity currentUser = userService.getCurrentUser(principal);
        byte[] pdf = protocolPdfService.generatePdf(protocolId, currentUser);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=protocol-" + protocolId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
