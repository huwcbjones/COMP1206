package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Worker Pool for Running Tasks
 *
 * @author Huw Jones
 * @since 28/03/2016
 */
public class WorkerPool {

    private static final Logger log = LogManager.getLogger(WorkerPool.class);

    private final ScheduledExecutorService workerPool;

    public WorkerPool(int workers) {
        this.workerPool = Executors.newScheduledThreadPool(workers);
        log.debug("Started worker pool: {}", workers);
    }

    /**
     * Runs a task asynchronously in the worker pool
     *
     * @param task Task to run
     */
    public void queueTask (Runnable task) {
        log.debug("Queuing task ({}) to run now", task.toString());
        this.workerPool.submit(task);
    }

    /**
     * Runs a task asynchronously in the worker pool
     *
     * @param task Task to run
     */
    public void queueTask(Callable task) {
        log.debug("Queuing task ({}) to run now", task.toString());
        this.workerPool.submit(task);
    }

    /**
     * Schedules a task to run after a delay
     *
     * @param task      Task to run
     * @param timeDelay Time to delay task (in milliseconds);
     */
    public void scheduleTask(Runnable task, long timeDelay) {
        log.debug("Scheduling task ({}) to run in {}ms", task.toString(), timeDelay);
        this.workerPool.schedule(task, timeDelay, TimeUnit.MILLISECONDS);
    }

    /**
     * Schedules a task to run after a delay
     *
     * @param task      Task to run
     * @param timeDelay Time to delay task (in milliseconds);
     */
    public void scheduleTask(Callable task, long timeDelay) {
        log.debug("Scheduling task ({}) to run in {}ms", task.toString(), timeDelay);
        this.workerPool.schedule(task, timeDelay, TimeUnit.MILLISECONDS);
    }

    public void shutdown(){
        log.info("Shutting down worker pool...");
        workerPool.shutdown();
        try {
            log.info("Waiting for tasks to complete...");
            this.workerPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch(InterruptedException ignored){
        }
        log.info("Worker pool shutdown!");
    }
}
