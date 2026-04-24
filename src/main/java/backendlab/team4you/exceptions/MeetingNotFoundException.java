package backendlab.team4you.exceptions;

public class MeetingNotFoundException extends RuntimeException {

    public MeetingNotFoundException(Long meetingId) {
        super("Sammanträdet hittades inte.");
    }
}
