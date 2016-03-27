package shared.exceptions;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 26/03/2016
 */
public class PasswordNotStrongEnoughException extends Exception {

    public PasswordNotStrongEnoughException(){
        super("Password must contain lower case, upper case and a number, or be longer than 32 characters.");
    }
}
