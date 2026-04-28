package backendlab.team4you.exceptions;

public class ProtocolNotFoundException extends RuntimeException {

    public ProtocolNotFoundException(Long protocolId) {
        super("Protocol not found. protocolId=" + protocolId);
    }
}
