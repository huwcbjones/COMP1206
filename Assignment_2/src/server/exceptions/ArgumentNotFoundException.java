package server.exceptions;

/**
 * Thrown if an argument was not found
 *
 * @author Huw Jones
 * @since 26/03/2016
 */
public class ArgumentNotFoundException extends Exception {
    public ArgumentNotFoundException(String message){
        super(message);
    }
}
