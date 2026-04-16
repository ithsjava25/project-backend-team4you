package backendlab.team4you.exceptions;

public class DuplicateRegistryCodeException extends RuntimeException {
    public DuplicateRegistryCodeException(String message) {
        super(message);
    }
}
