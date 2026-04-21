package backendlab.team4you.casefile;

public record CaseFileListItemDto(
        Long id,
        String documentReference,
        String displayName,
        boolean confidential,
        boolean canDownload
) {
}
