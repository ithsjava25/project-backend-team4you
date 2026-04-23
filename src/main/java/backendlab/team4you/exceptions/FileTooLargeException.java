package backendlab.team4you.exceptions;

public class FileTooLargeException extends RuntimeException {
    public FileTooLargeException(long maxBytes) {
        super("Filen är för stor. Maxstorlek är " + toMegabytesRoundedUp(maxBytes) + " MB.");
        }
        private static long toMegabytesRoundedUp(long bytes) {
        long bytesPerMegabyte = 1024L * 1024L;
        return Math.max(1L, (bytes + bytesPerMegabyte - 1L) / bytesPerMegabyte);
    }
}
