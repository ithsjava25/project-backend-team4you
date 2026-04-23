package backendlab.team4you.exceptions;

public class DuplicateMeetingAgendaDocumentException extends RuntimeException {
    public DuplicateMeetingAgendaDocumentException(String message) {
        super(message);
    }
}
