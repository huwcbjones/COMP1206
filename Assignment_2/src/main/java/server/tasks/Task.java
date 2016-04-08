package server.tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.ClientConnection;

/**
 * A template abstract task
 *
 * @author Huw Jones
 * @since 08/04/2016
 */
abstract class Task implements Runnable {

    protected static final Logger log = LogManager.getLogger(Task.class);
    protected final ClientConnection client;
    protected final String name;

    public Task(String taskName, ClientConnection client) {
        this.name = taskName;
        this.client = client;
    }

    @Override
    public final String toString() {
        return this.name;
    }
}
