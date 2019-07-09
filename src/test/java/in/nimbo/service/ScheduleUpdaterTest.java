package in.nimbo.service;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import in.nimbo.TestUtility;
import in.nimbo.entity.Entry;
import in.nimbo.entity.Site;
import in.nimbo.exception.SyndFeedException;
import in.nimbo.service.schedule.ScheduleUpdater;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
public class ScheduleUpdaterTest {
    private static ScheduledExecutorService scheduledService;
    private static RSSService rssService;

    @BeforeClass
    public static void init() {
        rssService = PowerMockito.mock(RSSService.class);
        scheduledService = PowerMockito.mock(ScheduledExecutorService.class);
    }

    @Test
    public void getNewPublicationDates() {
        Site site = new Site("site name", "site link");

        SyndFeed syndFeed = new SyndFeedImpl();
        List<Entry> entries = new ArrayList<>();
        List<LocalDateTime> dateTimes = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            LocalDateTime dateTime = LocalDateTime.of(2000, 1, i, 0, 0);
            dateTimes.add(dateTime);
            entries.add(TestUtility.createEntry("channel " + i,
                    "title " + i, "link " + i, dateTime, "content " + i, "desc " + i));
        }
        PowerMockito.when(rssService.fetchFromURL(site.getLink())).thenReturn(syndFeed);
        PowerMockito.when(rssService.getEntries(syndFeed)).thenReturn(entries);
        PowerMockito.when(rssService.addSiteEntries(site.getLink(), entries)).thenReturn(entries);

        ScheduleUpdater scheduleUpdater = new ScheduleUpdater(scheduledService, rssService, site, 5);
        List<LocalDateTime> newPublicationDates = scheduleUpdater.getNewPublicationDates();
        assertEquals(dateTimes, newPublicationDates);

        PowerMockito.when(rssService.fetchFromURL(site.getLink())).thenThrow(new SyndFeedException());
        newPublicationDates = scheduleUpdater.getNewPublicationDates();
        assertEquals(0, newPublicationDates.size());
    }

    @Test
    public void getSumOfIntervals() {
        Site site = new Site("site name", "site link");
        Site siteWithLastUpdate = new Site("site name", "site link");
        siteWithLastUpdate.setLastUpdate(LocalDateTime.of(1999, 1, 1, 0, 0));
        siteWithLastUpdate.setNewsCount(2);
        siteWithLastUpdate.setAvgUpdateTime(1);

        List<LocalDateTime> dateTimes = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            LocalDateTime dateTime = LocalDateTime.of(2000, 1, i, 0, 0);
            dateTimes.add(dateTime);
        }

        ScheduleUpdater scheduleUpdater = new ScheduleUpdater(scheduledService, rssService, site, 5);
        long sumOfIntervals = scheduleUpdater.getSumOfIntervals(dateTimes);
        long realSumOfIntervals = 0;
        for (int i = 0; i < dateTimes.size() - 1; i++) {
            realSumOfIntervals += dateTimes.get(i).until(dateTimes.get(i + 1), ChronoUnit.MILLIS);
        }
        assertEquals(realSumOfIntervals, sumOfIntervals);

        scheduleUpdater = new ScheduleUpdater(scheduledService, rssService, siteWithLastUpdate, 5);
        sumOfIntervals = scheduleUpdater.getSumOfIntervals(dateTimes);
        realSumOfIntervals += siteWithLastUpdate.getLastUpdate().until(dateTimes.get(0), ChronoUnit.MILLIS);
        assertEquals(realSumOfIntervals, sumOfIntervals);
    }
}
