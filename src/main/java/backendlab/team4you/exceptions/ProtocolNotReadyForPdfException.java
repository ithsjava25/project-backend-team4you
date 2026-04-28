package backendlab.team4you.exceptions;

public class ProtocolNotReadyForPdfException extends RuntimeException {
    private final Long protocolId;

    public ProtocolNotReadyForPdfException(Long protocolId) {
        super("Alla paragrafer måste ha beslut och beslutstext innan PDF kan skapas.");
        this.protocolId = protocolId;
    }

    public Long getProtocolId() {
        return protocolId;
    }
}
