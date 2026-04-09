package backendlab.team4you.exceptions;

public class RegistryNotFoundException extends RuntimeException {
    public RegistryNotFoundException(Long userId) {
    super("user not found: " + userId);
    }
}
