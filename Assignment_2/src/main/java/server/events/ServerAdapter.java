package server.events;

/**
 * Adapter for ServerListener
 *
 * @author Huw Jones
 * @since 22/04/2016
 */
public abstract class ServerAdapter implements ServerListener {
    /**
     * Fired when server starts up
     */
    @Override
    public void serverStarting() {

    }

    /**
     * Fired when server starts
     */
    @Override
    public void serverStarted() {

    }

    /**
     * Fired when server initiates shutdown
     */
    @Override
    public void serverShuttingDown() {

    }

    /**
     * Fired when server shuts down
     */
    @Override
    public void serverShutdown() {

    }
}
