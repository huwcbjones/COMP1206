package shared.exceptions;

/**
 * Thrown when client/server version mismatch
 *
 * @author Huw Jones
 * @since 26/03/2016
 */
public class VersionMismatchException extends Exception {
    public VersionMismatchException() {
        super("Client/Server version mismatch.");
    }

    public VersionMismatchException(String message) {
        super(message);
    }

    public VersionMismatchException(String message, Throwable cause){
        super(message, cause);
    }

    public static VersionMismatchException FromException(Exception ex){
        VersionMismatchException exception = new VersionMismatchException(ex.getMessage(), ex.getCause());
        return exception;
    }
}
