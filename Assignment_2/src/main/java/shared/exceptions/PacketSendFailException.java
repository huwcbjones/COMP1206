package shared.exceptions;

/**
 * Thrown if a Packet failed to send
 *
 * @author Huw Jones
 * @since 26/03/2016
 */
public class PacketSendFailException extends Exception {

    public PacketSendFailException () {
        super("An error occurred whilst sending packet. Packet failed to send.");
    }

    public PacketSendFailException (String message) {
        super(message);
    }

    public PacketSendFailException (String message, Throwable cause){
        super(message, cause);
    }

    public static PacketSendFailException FromException(Exception ex){
        PacketSendFailException exception = new PacketSendFailException(ex.getMessage(), ex.getCause());
        return exception;
    }
}
