package backendlab.team4you.exceptions;

public class ProtocolAlreadyArchivedException extends RuntimeException {
    private final Long protocolId;

    public ProtocolAlreadyArchivedException(Long protocolId) {
        super("Protokollet är redan arkiverat och kan inte längre ändras.");
        this.protocolId = protocolId;
    }

    public Long getProtocolId() {
        return protocolId;
    }
}
