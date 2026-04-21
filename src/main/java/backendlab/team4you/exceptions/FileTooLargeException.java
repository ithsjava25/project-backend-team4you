package backendlab.team4you.exceptions;

public class FileTooLargeException extends RuntimeException {
    public FileTooLargeException(Long maxBytes) {
        super("Filen är för stor. Maxstorlek är " + (maxBytes / (1024 * 1024)) + " MB.");
    }
}
