package backendlab.team4you.exceptions;

public class DuplicateRegistryNameException extends RuntimeException {
    public DuplicateRegistryNameException(String message) {
        super(message);
    }
}
