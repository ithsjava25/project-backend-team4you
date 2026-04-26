package backendlab.team4you.caserecord;


import backendlab.team4you.registryaccess.RegistryAccessService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@RestController
@RequestMapping("/api/case-records")
public class CaseRecordController {

    private final CaseRecordService caseRecordService;
    private final RegistryAccessService registryAccessService;


    public CaseRecordController(
            CaseRecordService caseRecordService,
            RegistryAccessService registryAccessService
    ) {
        this.caseRecordService = caseRecordService;
        this.registryAccessService = registryAccessService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CaseRecordResponseDto> createCaseRecord(
            @Valid @RequestBody CaseRecordRequestDto requestDto,
            Authentication authentication
    ) {

        String username = authentication.getName();

        boolean allowed = registryAccessService
                .canCreateCasesInRegistry(
                        username,
                        requestDto.registryId()
                );

        if (!allowed) {
            throw new ResponseStatusException(
                    FORBIDDEN,
                    "You do not have permission to create cases in this registry"
            );
        }

        CaseRecordResponseDto responseDto =
                caseRecordService.createCaseRecord(requestDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(responseDto.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(responseDto);
    }
}