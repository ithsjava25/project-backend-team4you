package backendlab.team4you.exceptions;

public class ProtocolAlreadyExistsException extends RuntimeException {

    public ProtocolAlreadyExistsException(Long meetingId) {
        super("Protocol already exists for meeting. meetingId=" + meetingId);
    }
}
