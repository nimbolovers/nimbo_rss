package in.nimbo.service.schedule;

import com.rometools.rome.feed.synd.SyndFeed;
import in.nimbo.entity.Entry;
import in.nimbo.entity.Site;
import in.nimbo.exception.CalculateAverageUpdateException;
import in.nimbo.exception.QueryException;
import in.nimbo.exception.RssServiceException;
import in.nimbo.exception.SyndFeedException;
import in.nimbo.service.RSSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
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
    private static final long DEFAULT_UPDATE_INTERVAL = 5;

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
        if (updateInterval == 0)
            this.updateInterval = DEFAULT_UPDATE_INTERVAL;
    }

    @Override
    public Void call() {
        try {
            List<Entry> newEntries = getNewEntries();

            if (newEntries.isEmpty())
                throw new CalculateAverageUpdateException("There is no entry with publication date to calculate average time");

            List<Date> pubDates = newEntries.stream().map(Entry::getPublicationDate)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (pubDates.isEmpty())
                throw new CalculateAverageUpdateException("There is no entry with publication date to calculate average time");

            // sort entries based on their publication date
            pubDates.sort(Date::compareTo);

            long sumOfIntervals = IntStream.range(0, pubDates.size() - 1)
                    .mapToLong(i -> pubDates.get(i + 1).getTime() - pubDates.get(i).getTime())
                    .sum();
            if (site.getLastUpdate() != null)
                sumOfIntervals += pubDates.get(0).getTime() - site.getLastUpdate().getTime();

            long newAverageUpdateTime;
            if (site.getAvgUpdateTime() > 0) {
                // number of news before adding new news
                long lastNewsCount = site.getNewsCount() - newEntries.size();
                newAverageUpdateTime = (site.getAvgUpdateTime() * lastNewsCount + sumOfIntervals) / (lastNewsCount + newEntries.size());
            } else
                newAverageUpdateTime = sumOfIntervals / newEntries.size();

            newAverageUpdateTime /= 1000; //convert milliseconds to seconds
            if (newAverageUpdateTime <= 0)
                newAverageUpdateTime = DEFAULT_UPDATE_INTERVAL;

            site.setAvgUpdateTime(newAverageUpdateTime);
            site.setLastUpdate(pubDates.get(pubDates.size() - 1));

            updateInterval = newAverageUpdateTime;
        } catch (CalculateAverageUpdateException e) {
            updateInterval *= 2;
        }

        if (updateInterval > 60L * 60L) // more than one hour
            updateInterval = 3L * 60L * 60L; // set to 3 hours

        try {
            rssService.updateSite(site);
        } catch (RssServiceException e) {
            logger.error(e.getMessage());
        }

        scheduledService.schedule(this, updateInterval, TimeUnit.SECONDS);
        return null;
    }

    /**
     * fetch entries of this.site and only new entries to database
     *
     * @return only new entries
     */
    private List<Entry> getNewEntries() {
        try {
            SyndFeed syndFeed = rssService.fetchFromURL(site.getLink());
            List<Entry> entries = rssService.getEntries(syndFeed);
            return rssService.addSiteEntries(site, entries);
        } catch (SyndFeedException | QueryException e) {
            logger.warn(e.getMessage());
            return new ArrayList<>();
        }
    }
}
