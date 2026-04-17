package backendlab.team4you.exceptions;

public class CaseFileNotFoundException extends RuntimeException {
    public CaseFileNotFoundException(Long caseRecordId, Long fileId) {
        super("File not found for case record. caseRecordId=" + caseRecordId + ", fileId=" + fileId);
    }
}
