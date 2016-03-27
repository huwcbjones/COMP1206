package shared.exceptions;

/**
 * Thrown when two passwords do not match
 *
 * @author Huw Jones
 * @since 26/03/2016
 */
public class PasswordsDoNotMatchException extends Exception {
    public PasswordsDoNotMatchException () {
        super("Passwords do not match.");
    }
}
