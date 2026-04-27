package backendlab.team4you.exceptions;

public class MeetingNotFoundException extends RuntimeException {
    private final Long meetingId;

    public MeetingNotFoundException(Long meetingId) {
        super("Sammanträdet hittades inte.");
        this.meetingId = meetingId;
    }

    public Long getMeetingId() {
        return meetingId;
    }
}
