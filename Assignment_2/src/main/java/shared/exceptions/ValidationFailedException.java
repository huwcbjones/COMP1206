package shared.exceptions;

/**
 * Thrown when validation failed
 *
 * @author Huw Jones
 * @since 26/03/2016
 */
public class ValidationFailedException extends Exception {

    public ValidationFailedException(String message){
        super(message);
    }
}
