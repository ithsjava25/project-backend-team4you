package backendlab.team4you.exceptions;

public class FileKeyConflictException extends RuntimeException {
    private static final String MESSAGE = "A file with the same name already exists.";
    public FileKeyConflictException(String key) {
        super(MESSAGE);
    }

    public FileKeyConflictException(String key, Throwable cause) {
        super(MESSAGE, cause);
    }
}
