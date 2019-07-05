package in.nimbo.service.schedule;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import in.nimbo.entity.Entry;
import in.nimbo.entity.Site;
import in.nimbo.service.RSSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ScheduleUpdater implements Callable<Void> {
    private Logger logger = LoggerFactory.getLogger(ScheduleUpdater.class);

    private Site site;
    private ScheduledExecutorService scheduledService;
    private RSSService rssService;

    /**
     * represent interval at which fetch RSS link for new entries
     * unit of it is seconds
     */
    private long updateInterval;

    public ScheduleUpdater(Site site, ScheduledExecutorService scheduledService, RSSService rssService, long updateInterval) {
        this.site = site;
        this.scheduledService = scheduledService;
        this.rssService = rssService;
        this.updateInterval = updateInterval;
    }

    @Override
    public Void call() {
        List<Entry> newEntries = null;
        try {
            SyndFeed syndFeed = rssService.fetchFromURL(site.getLink());
            newEntries = rssService.addSiteEntries(site, syndFeed);
        } catch (RuntimeException e) {
            // unable to fetch data from url
            // so we will try it later
        }

        if (newEntries != null && !newEntries.isEmpty()) {
            // sort entries based on their publication date
            newEntries.sort(Comparator.comparing(o -> o.getSyndEntry().getPublishedDate()));

            List<Date> pubDates = newEntries.stream().map(Entry::getSyndEntry)
                    .map(SyndEntry::getPublishedDate)
                    .collect(Collectors.toList());
            long sumOfIntervals = IntStream.range(0, pubDates.size() - 1)
                    .mapToLong(i -> pubDates.get(i + 1).getTime() - pubDates.get(i).getTime())
                    .sum();
            if (site.getLastUpdate() != null)
                sumOfIntervals += pubDates.get(0).getTime() - site.getLastUpdate().getTime();

            long newAverageUpdateTime;
            if (site.getAvgUpdateTime() > 0)
                newAverageUpdateTime = (site.getAvgUpdateTime() * site.getNewsCount() + sumOfIntervals) / (site.getNewsCount() + newEntries.size());
            else
                newAverageUpdateTime = sumOfIntervals / newEntries.size();
            site.setAvgUpdateTime(newAverageUpdateTime);
            site.setLastUpdate(pubDates.get(pubDates.size() - 1));
            site.increaseNewsCount(newEntries.size());

            updateInterval = newAverageUpdateTime;
        } else {
            updateInterval *= 2;
        }

        if (updateInterval > 60 * 60) // more than one hour
            updateInterval = 3 * 60 * 60; // set to 3 hours

        scheduledService.schedule(this, updateInterval, TimeUnit.SECONDS);
        return null;
    }
}
