package backendlab.team4you.exceptions;

public class RegistryNotFoundException extends RuntimeException {
    public RegistryNotFoundException(String message) {
        super(message);
    }
}
