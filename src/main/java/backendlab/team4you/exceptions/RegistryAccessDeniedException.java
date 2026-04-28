package backendlab.team4you.exceptions;

public class RegistryAccessDeniedException
        extends RuntimeException {

    public RegistryAccessDeniedException(String message) {
        super(message);
    }
}
