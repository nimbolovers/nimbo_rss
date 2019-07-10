package in.nimbo.service.schedule;

import com.rometools.rome.feed.synd.SyndFeed;
import in.nimbo.entity.Entry;
import in.nimbo.entity.Site;
import in.nimbo.exception.QueryException;
import in.nimbo.exception.SyndFeedException;
import in.nimbo.service.RSSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * get news frequently from rss link
 */
public class ScheduleUpdater implements Callable<Void> {
    private Logger logger = LoggerFactory.getLogger(ScheduleUpdater.class);
    public static final long DEFAULT_UPDATE_INTERVAL = 5;

    private Site site;
    private ScheduledExecutorService scheduledService;
    private RSSService rssService;

    /**
     * represent interval at which fetch RSS link for new entries
     * unit of it is seconds
     */
    private long updateInterval;

    /**
     * number of news before adding new entries
     */
    private long lastNewsCount;

    public ScheduleUpdater(ScheduledExecutorService scheduledService, RSSService rssService,
                           Site site, long updateInterval, long lastNewsCount) {
        this.site = site;
        this.scheduledService = scheduledService;
        this.rssService = rssService;
        this.lastNewsCount = lastNewsCount;
        this.updateInterval = updateInterval;
        if (updateInterval == 0)
            this.updateInterval = DEFAULT_UPDATE_INTERVAL;
    }

    @Override
    public Void call() {
        calculateUpdateInterval();
        scheduledService.schedule(this, updateInterval, TimeUnit.SECONDS);
        return null;
    }

    public long getUpdateInterval() {
        return updateInterval;
    }

    /**
     * calculate new update interval based on new news published to site
     * if there is no new entry, update time is doubled (never exceed 3 hours)
     */
    public void calculateUpdateInterval() {
        List<LocalDateTime> pubDates = getNewPublicationDates();
        if (pubDates.isEmpty()) {
            updateInterval *= 2;
        } else {
            long newAverageUpdateTime = getNewAverageUpdateTime(pubDates);
            site.setAvgUpdateTime(newAverageUpdateTime);
            site.setLastUpdate(pubDates.get(pubDates.size() - 1));
            updateInterval = newAverageUpdateTime;
        }
        if (updateInterval > 60L * 60L)
            updateInterval = 3L * 60L * 60L;
    }

    /**
     * calculate new average update time based on new publication date of site and last news in site
     * @param pubDates list of new entries' publication date (not empty)
     * @return new average update time
     */
    public long getNewAverageUpdateTime(List<LocalDateTime> pubDates) {
        long sumOfIntervals = getSumOfIntervals(pubDates);
        long newAverageUpdateTime;
        if (site.getLastUpdate() == null) {
            newAverageUpdateTime = sumOfIntervals / site.getNewsCount();
        } else {
            newAverageUpdateTime = (site.getAvgUpdateTime() * lastNewsCount + sumOfIntervals) / (site.getNewsCount());
        }
        newAverageUpdateTime /= 1000;
        if (newAverageUpdateTime <= 0)
            newAverageUpdateTime = DEFAULT_UPDATE_INTERVAL;
        return newAverageUpdateTime;
    }

    /**
     * sum of distance between each two date in list of dates
     * if site has an last update time, then distance of first new date and last update date is calculated too
     *
     * @param pubDates list of publication dates
     * @return sum of distance between dates
     */
    public long getSumOfIntervals(List<LocalDateTime> pubDates) {
        pubDates.sort(LocalDateTime::compareTo);

        long sumOfIntervals = IntStream.range(0, pubDates.size() - 1)
                .mapToLong(i -> pubDates.get(i).until(pubDates.get(i + 1), ChronoUnit.MILLIS))
                .sum();
        if (site.getLastUpdate() != null)
            sumOfIntervals += site.getLastUpdate().until(pubDates.get(0), ChronoUnit.MILLIS);
        return sumOfIntervals;
    }

    /**
     * fetch entries of site and add only new entries to database
     *
     * @return publication date of new entries
     *         empty list if there is no new entries
     */
    public List<LocalDateTime> getNewPublicationDates() {
        try {
            SyndFeed syndFeed = rssService.fetchFeedFromURL(site.getLink());
            List<Entry> entries = rssService.getEntries(syndFeed);
            List<Entry> newEntries = rssService.addSiteEntries(site.getLink(), entries);
            lastNewsCount = site.getNewsCount();
            site.increaseNewsCount(newEntries.size());
            return newEntries.stream().map(Entry::getPublicationDate)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (SyndFeedException | QueryException e) {
            logger.warn(e.getMessage());
            return new ArrayList<>();
        }
    }
}
