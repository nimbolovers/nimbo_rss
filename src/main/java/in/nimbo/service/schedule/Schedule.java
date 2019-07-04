package in.nimbo.service.schedule;

import in.nimbo.service.RSSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.function.Supplier;

public class Schedule {
    private Logger logger = LoggerFactory.getLogger(Schedule.class);
    private ScheduledExecutorService scheduleService;
    private RSSService rssService;

    public Schedule(RSSService rssService) {
        this.rssService = rssService;
        scheduleService = Executors.newScheduledThreadPool(100);

    }

    private <T> void scheduleWithTimeout(Supplier<T> supplier, long timeout, TimeUnit unit, String timeoutMessage) {
        CompletableFuture<T> taskCF = new CompletableFuture<>();

        Future<?> future = scheduleService.submit(() -> {
            try {
                taskCF.complete(supplier.get());
            } catch (Throwable ex) {
                taskCF.completeExceptionally(ex);
            }
        });

        // schedule watcher for timeout
        scheduleService.schedule(new ScheduleWatcher<>(timeoutMessage, taskCF, future), timeout, unit);
    }

    public void scheduleRSSLink(String link) {
        Supplier<Void> voidCompletableFuture = () -> {
            rssService.save(rssService.fetchFromURL(link));
            return null;
        };
        scheduleWithTimeout(voidCompletableFuture, 1L, TimeUnit.MILLISECONDS, "Unable to fetch data from link: " + link);
    }
}
