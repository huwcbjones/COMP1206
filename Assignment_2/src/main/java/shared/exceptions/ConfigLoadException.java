package shared.exceptions;

/**
 * Thrown if there was an error whilst loading the config
 *
 * @author Huw Jones
 * @since 26/03/2016
 */
public class ConfigLoadException extends Exception {

    public ConfigLoadException (String message) {
        super(message);
    }
}
