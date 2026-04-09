package backendlab.team4you.caserecord;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/case-records")
public class CaseRecordController {
    private final CaseRecordService caseRecordService;

    public CaseRecordController(CaseRecordService caseRecordService) {
        this.caseRecordService = caseRecordService;
    }

    @PostMapping
    public ResponseEntity<CaseRecordResponseDto> createCaseRecord(
            @Valid @RequestBody CaseRecordRequestDto requestDto) {

        CaseRecordResponseDto responseDto = caseRecordService.createCaseRecord(requestDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(responseDto.id())
                .toUri();

        return ResponseEntity.created(location).body(responseDto);
    }
}
