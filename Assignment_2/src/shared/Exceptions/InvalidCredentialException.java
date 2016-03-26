package shared.exceptions;

/**
 * Thrown when invalid credentials provided
 *
 * @author Huw Jones
 * @since 26/03/2016
 */
public class InvalidCredentialException extends Exception {

    public InvalidCredentialException (String message) {
        super(message);
    }
}
