package backendlab.team4you.exceptions;

public class ProtocolParagraphNotFoundException extends RuntimeException {

    public ProtocolParagraphNotFoundException(Long paragraphId) {
        super("Protocol paragraph not found. paragraphId=" + paragraphId);
    }
}
