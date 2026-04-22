package backendlab.team4you.exceptions;

public class CaseRecordNotFoundException extends RuntimeException {
    public CaseRecordNotFoundException(Long caseRecordId) {
        super("Case record not found: " + caseRecordId);
    }
}
