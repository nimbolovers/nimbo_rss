package in.nimbo.service.schedule;

import in.nimbo.entity.Site;
import in.nimbo.service.RSSService;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Schedule {
    private ScheduledExecutorService scheduleService;
    private RSSService rssService;

    public Schedule(RSSService rssService) {
        this.rssService = rssService;
        scheduleService = Executors.newScheduledThreadPool(100);
    }

    /**
     * schedule a site to get news frequently when site publish new news
     * @param site site to update news
     */
    public void scheduleSite(Site site) {
        scheduleService.schedule(new ScheduleUpdater(scheduleService, rssService, site, site.getAvgUpdateTime(), site.getNewsCount()), 5L, TimeUnit.SECONDS);
    }

    /**
     * schedule for save and update a site frequently
     * @param sites sites to update frequently
     */
    public void scheduleSiteDAO(List<Site> sites) {
        scheduleService.scheduleAtFixedRate(new ScheduleSiteUpdater(sites, rssService), 5L, 15L * 60L, TimeUnit.SECONDS);
    }

    /**
     * stop schedule service from getting more information
     */
    public void stopService() {
        scheduleService.shutdownNow();
    }
}
