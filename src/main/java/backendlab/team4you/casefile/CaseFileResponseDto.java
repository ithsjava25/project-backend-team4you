package backendlab.team4you.casefile;

import java.time.LocalDateTime;

public record CaseFileResponseDto(
        Long id,
        String originalFilename,
        String contentType,
        long size,
        LocalDateTime uploadedAt
) {
    public static CaseFileResponseDto from(CaseFile caseFile) {
        return new CaseFileResponseDto(
                caseFile.getId(),
                caseFile.getOriginalFilename(),
                caseFile.getContentType(),
                caseFile.getSize(),
                caseFile.getUploadedAt()
        );
    }
}
