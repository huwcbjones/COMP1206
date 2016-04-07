package shared.events;

import java.util.EventListener;

/**
 * Event handler for connection events
 *
 * @author Huw Jones
 * @since 28/03/2016
 */
public interface ConnectionListener extends EventListener {

    /**
     * Fires when the connection succeeds
     */
    void connectionSucceeded();

    /**
     * Fires when the connection fails
     * @param reason Reason why connection failed
     */
    void connectionFailed(String reason);

    /**
     * Fires when the connection is closed
     * @param reason Reason why the connection is closed
     */
    void connectionClosed(String reason);
}
