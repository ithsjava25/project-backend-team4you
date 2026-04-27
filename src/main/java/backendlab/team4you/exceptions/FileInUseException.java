package backendlab.team4you.exceptions;

public class FileInUseException extends RuntimeException {
    public FileInUseException(String message) {
        super(message);
    }
}
