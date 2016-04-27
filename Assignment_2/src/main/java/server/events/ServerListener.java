package server.events;

import java.util.EventListener;

/**
 * Event Handler for Server Events
 *
 * @author Huw Jones
 * @since 22/04/2016
 */
public interface ServerListener extends EventListener {

    /**
     * Fired when server starts up
     */
    void serverStarting();

    /**
     * Fired when server starts
     */
    void serverStarted();

    /**
     * Fired when server initiates shutdown
     */
    void serverShuttingDown();

    /**
     * Fired when server shuts down
     */
    void serverShutdown();

    /**
     * Fired when the server fails to start
     *
     * @param reason Why the server failed to start
     */
    void serverStartFail(String reason);
}
