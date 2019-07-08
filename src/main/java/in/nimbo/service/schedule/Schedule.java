package in.nimbo.service.schedule;

import in.nimbo.entity.Site;
import in.nimbo.service.RSSService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Schedule {
    private static ScheduledExecutorService scheduleService;
    private RSSService rssService;

    public Schedule(RSSService rssService) {
        this.rssService = rssService;
        scheduleService = Executors.newScheduledThreadPool(100);
    }

    public void scheduleSite(Site site) {
        scheduleService.schedule(new ScheduleUpdater(site, scheduleService, rssService, site.getAvgUpdateTime()), 5L, TimeUnit.SECONDS);
    }

    /**
     * stop schedule service from getting more information
     */
    public void stopService() {
        scheduleService.shutdownNow();
    }
}
