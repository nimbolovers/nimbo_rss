package in.nimbo.service.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class ScheduleWatcher<T> implements Callable<Void> {
    private Logger logger = LoggerFactory.getLogger(ScheduleWatcher.class);

    /**
     * task which is watched
     */
    private CompletableFuture<T> task;

    /**
     * future which run task internally
     * it is used for throw interruption and stop task
     */
    private Future<?> wrappedFuture;
    private String timeoutMessage;

    public ScheduleWatcher(String timeoutMessage, CompletableFuture<T> task, Future<?> wrappedFuture) {
        this.timeoutMessage = timeoutMessage;
        this.task = task;
        this.wrappedFuture = wrappedFuture;
    }

    @Override
    public Void call() throws TimeoutException {
//        if (task.isCompletedExceptionally()) {
//            logger.warn(timeoutMessage);
//            throw new TimeoutException();
//        }
        if (!task.isDone()) {
            task.completeExceptionally(new TimeoutException());
            wrappedFuture.cancel(true);
//            try {
//                TimeUnit.MILLISECONDS.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            logger.warn(timeoutMessage);
//            throw new TimeoutException();
        }
        return null;
    }
}
