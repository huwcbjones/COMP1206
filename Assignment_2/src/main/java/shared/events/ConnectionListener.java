package shared.events;

/**
 * Event handler for connection events
 *
 * @author Huw Jones
 * @since 28/03/2016
 */
public interface ConnectionListener {

    void connectionClosed(String reason);
}
