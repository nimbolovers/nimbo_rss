package in.nimbo.service.schedule;

import in.nimbo.entity.Site;
import in.nimbo.service.RSSService;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Schedule {
    private static ScheduledExecutorService scheduleService;
    private RSSService rssService;
    private List<Site> sites;

    public Schedule(RSSService rssService, List<Site> sites) {
        this.rssService = rssService;
        this.sites = sites;
        scheduleService = Executors.newScheduledThreadPool(100);
    }

    public List<Site> getSites() {
        return sites;
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
