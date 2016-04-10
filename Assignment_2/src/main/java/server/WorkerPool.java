package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Worker Pool for Running Tasks
 *
 * @author Huw Jones
 * @since 28/03/2016
 */
public class WorkerPool {

    private static final Logger log = LogManager.getLogger(WorkerPool.class);

    private final ArrayList<ScheduledFuture> futureTasks = new ArrayList<>();
    private final ScheduledExecutorService workerPool;

    public WorkerPool(int workers) {
        ThreadFactory factory = new WorkerPoolFactory();
        this.workerPool = Executors.newScheduledThreadPool(workers, factory);
        log.debug("Started worker pool: {}", workers);
    }

    /**
     * Runs a task asynchronously in the worker pool
     *
     * @param task Task to run
     */
    public void queueTask(Runnable task) {
        if(this.workerPool.isShutdown()){
            log.warn("Failed to queue task ({}). Worker Pool shutting down...", task.toString());
            return;
        }
        log.debug("Queuing task ({}) to run now", task.toString());
        this.workerPool.submit(task);
    }

    /**
     * Runs a task asynchronously in the worker pool
     *
     * @param task Task to run
     */
    public void queueTask(Callable task) {
        if(this.workerPool.isShutdown()){
            log.warn("Failed to queue task ({}). Worker Pool shutting down...", task.toString());
            return;
        }
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
        if(this.workerPool.isShutdown()){
            log.warn("Failed to schedule task ({}). Worker Pool shutting down...", task.toString());
            return;
        }
        log.debug("Scheduling task ({}) to run in {}ms", task.toString(), timeDelay);
        ScheduledFuture futureTask = this.workerPool.schedule(task, timeDelay, TimeUnit.MILLISECONDS);
        this.futureTasks.add(futureTask);
    }

    /**
     * Schedules a task to run after a delay
     *
     * @param task      Task to run
     * @param timeDelay Time to delay task (in milliseconds);
     */
    public void scheduleTask(Callable task, long timeDelay) {
        if(this.workerPool.isShutdown()){
            log.warn("Failed to schedule task ({}). Worker Pool shutting down...", task.toString());
            return;
        }
        log.debug("Scheduling task ({}) to run in {}ms", task.toString(), timeDelay);
        ScheduledFuture futureTask = this.workerPool.schedule(task, timeDelay, TimeUnit.MILLISECONDS);
        this.futureTasks.add(futureTask);
    }

    public void shutdown() {
        log.info("Shutting down worker pool...");
        workerPool.shutdown();
        try {
            // Cancel queued tasks
            int cancelledTasks = 0;
            ArrayList<ScheduledFuture> tasks = new ArrayList<>(this.futureTasks);
            for(ScheduledFuture task : tasks){
                if(!task.isDone()){
                    task.cancel(false);
                    cancelledTasks++;
                }
            }
            log.info("Cancelled {} task(s).", cancelledTasks);

            // Wait for running tasks to finish
            ThreadPoolExecutor executor = (ThreadPoolExecutor) this.workerPool;
            long numberOfTasks;
            while ((numberOfTasks = executor.getActiveCount()) != 0) {
                log.info("There are {} task(s) running, waiting 5 seconds and trying again...", numberOfTasks);

                this.workerPool.awaitTermination(5 * 1000, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException ignored) {
        }
        log.info("Worker pool shutdown!");
    }

    /**
     * The default thread factory
     */
    private static class WorkerPoolFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        WorkerPoolFactory() {
            group = new ThreadGroup("WorkerPool");
            namePrefix = "WorkerPool-W";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                namePrefix + threadNumber.getAndIncrement(),
                0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
