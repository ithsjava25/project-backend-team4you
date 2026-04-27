package backendlab.team4you.exceptions;

public class ProtocolParagraphNotFoundException extends RuntimeException {
    private final Long paragraphId;

    public ProtocolParagraphNotFoundException(Long paragraphId) {
        super("Protokollparagrafen hittades inte.");
        this.paragraphId = paragraphId;
    }

    public Long getParagraphId() {
        return paragraphId;
    }
}
