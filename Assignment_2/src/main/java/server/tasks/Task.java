package server.tasks;

import server.ClientConnection;
import shared.utils.RunnableAdapter;

/**
 * A template abstract task
 *
 * @author Huw Jones
 * @since 08/04/2016
 */
abstract class Task extends RunnableAdapter {

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

    @Override
    public final void run() {
        try {
            this.runSafe();
        } catch (Exception e){
            log.error("An Exception occurred whilst executing {}.", this.name);
            log.catching(e);
            this.failureAction();
        }
    }

    /**
     * Executed on task failure
     */
    protected abstract void failureAction();
}
