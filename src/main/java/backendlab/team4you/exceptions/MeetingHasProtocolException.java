package backendlab.team4you.exceptions;

public class MeetingHasProtocolException extends RuntimeException {
    private final Long meetingId;

    public MeetingHasProtocolException(Long meetingId) {
        super("Sammanträdet har redan ett protokoll och kan inte längre ändras.");
        this.meetingId = meetingId;
    }

    public Long getMeetingId() {
        return meetingId;
    }
}
