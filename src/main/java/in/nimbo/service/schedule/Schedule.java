package in.nimbo.service.schedule;

import in.nimbo.entity.Site;
import in.nimbo.service.RSSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class Schedule {
    private Logger logger = LoggerFactory.getLogger(Schedule.class);
    private static ScheduledExecutorService scheduleService;
    private RSSService rssService;
    private List<Site> sites;

    public Schedule(RSSService rssService, List<Site> sites) {
        this.rssService = rssService;
        this.sites = sites;
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

//    public void scheduleRSSLink(String link) {
//        Supplier<Void> voidCompletableFuture = () -> {
//            rssService.save(rssService.fetchFromURL(link));
//            return null;
//        };
//        scheduleWithTimeout(voidCompletableFuture, 20L, TimeUnit.SECONDS, "Unable to fetch data from link: " + link);
//    }

    public void scheduleSite(Site site) {
        scheduleService.schedule(new ScheduleUpdater(site, scheduleService, rssService, 5), 5L, TimeUnit.SECONDS);
    }
}
