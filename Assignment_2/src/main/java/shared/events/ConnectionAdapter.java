package shared.events;

/**
 * Adapter for ConnectionListener
 *
 * @author Huw Jones
 * @since 04/04/2016
 */
public class ConnectionAdapter implements ConnectionListener {
    /**
     * Fires when the connection succeeds
     * Subclasses should override this method if they want to listen to this event.
     */
    @Override
    public void connectionSucceeded() {
    }

    /**
     * Fires when the connection fails
     * Subclasses should override this method if they want to listen to this event.
     *
     * @param reason Reason why connection failed
     */
    @Override
    public void connectionFailed(String reason) {
    }

    /**
     * Fires when the connection is closed
     * Subclasses should override this method if they want to listen to this event.
     *
     * @param reason Reason why the connection is closed
     */
    @Override
    public void connectionClosed(String reason) {
    }
}
