package server;

import java.util.concurrent.*;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 28/03/2016
 */
public class TaskScheduler {

    private ScheduledExecutorService service;

    public TaskScheduler (int workers) {
        this.service = Executors.newScheduledThreadPool(workers);
    }

    public void queueTask (Runnable task) {
        this.service.submit(task);
    }

    public void queueTask (Callable task) {
        this.service.submit(task);
    }
}
