package backendlab.team4you.exceptions;

public class FileKeyConflictException extends RuntimeException {
    public FileKeyConflictException(String key) {
        super("A file with the same name already exists.");
    }

    public FileKeyConflictException(String key, Throwable cause) {
        super("A file with the same name already exists: ", cause);
    }
}
