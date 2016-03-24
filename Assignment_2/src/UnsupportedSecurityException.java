/**
 * Thrown when performing a secure operation and the necessary level of security is not available
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public class UnsupportedSecurityException extends Exception {
    public UnsupportedSecurityException() {
        super("Operation failed. Platform is not secure enough to perform this operation.");
    }

    public UnsupportedSecurityException(String message) {
        super(message);
    }

    public UnsupportedSecurityException(String message, Throwable cause){
        super(message, cause);
    }

    public static UnsupportedSecurityException FromException(Exception ex){
        UnsupportedSecurityException exception = new UnsupportedSecurityException(ex.getMessage(), ex.getCause());
        return exception;
    }
}
