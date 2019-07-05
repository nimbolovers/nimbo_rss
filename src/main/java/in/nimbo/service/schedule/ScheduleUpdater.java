package in.nimbo.service.schedule;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import in.nimbo.entity.Entry;
import in.nimbo.entity.Site;
import in.nimbo.exception.CalculateAverageUpdateException;
import in.nimbo.service.RSSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
        try {
            List<Entry> newEntries;
            try {
                SyndFeed syndFeed = rssService.fetchFromURL(site.getLink());
                newEntries = rssService.addSiteEntries(site, syndFeed);
            } catch (RuntimeException e) {
                throw new CalculateAverageUpdateException("Unable to fetch data from url: " + site.getLink());
            }

            if (newEntries.isEmpty())
                throw new CalculateAverageUpdateException("There is no entry with publication date to calculate average time");

            List<Date> pubDates = newEntries.stream().map(Entry::getSyndEntry)
                    .map(SyndEntry::getPublishedDate)
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
                newAverageUpdateTime = (site.getAvgUpdateTime() * lastNewsCount  + sumOfIntervals) / (lastNewsCount + newEntries.size());
            } else
                newAverageUpdateTime = sumOfIntervals / newEntries.size();

            newAverageUpdateTime /= 1000; //convert milliseconds to seconds
            if (newAverageUpdateTime <= 0)
                newAverageUpdateTime = 1;

            site.setAvgUpdateTime(newAverageUpdateTime);
            site.setLastUpdate(pubDates.get(pubDates.size() - 1));

            updateInterval = newAverageUpdateTime;
        } catch (CalculateAverageUpdateException e) {
            updateInterval *= 2;
        }

        if (updateInterval > 60 * 60) // more than one hour
            updateInterval = 3 * 60 * 60; // set to 3 hours

        System.out.println(site);
        System.out.println("Update time: " + updateInterval);

        scheduledService.schedule(this, updateInterval, TimeUnit.SECONDS);
        return null;
    }
}
