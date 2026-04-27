package backendlab.team4you.exceptions;

public class DuplicateMeetingAgendaItemException extends RuntimeException {
    public DuplicateMeetingAgendaItemException(String message) {
        super(message);
    }
}
