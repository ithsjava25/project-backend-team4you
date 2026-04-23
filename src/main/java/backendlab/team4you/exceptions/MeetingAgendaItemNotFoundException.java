package backendlab.team4you.exceptions;

public class MeetingAgendaItemNotFoundException extends RuntimeException {
    public MeetingAgendaItemNotFoundException(String message) {
        super(message);
    }
}
