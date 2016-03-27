package shared.exceptions;

/**
 * Thrown when a connection fails
 *
 * @author Huw Jones
 * @since 27/03/2016
 */
public class ConnectionFailedException extends Exception {

    public ConnectionFailedException(String message){
        super("Connection failed. " + message);
    }

}
