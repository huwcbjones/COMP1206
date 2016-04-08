package server.exceptions;

/**
 * Thrown when an operation fails
 *
 * @author Huw Jones
 * @since 08/04/2016
 */
public class OperationFailureException extends Exception {

    public OperationFailureException(String message){
        super(message);
    }
}
