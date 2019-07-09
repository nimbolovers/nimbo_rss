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

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
public class ScheduleTest {
    private static ScheduledExecutorService scheduledService;
    private static RSSService rssService;

    @BeforeClass
    public static void init() {
        rssService = PowerMockito.mock(RSSService.class);
        scheduledService = PowerMockito.mock(ScheduledExecutorService.class);
    }

    private Site getExampleSite() {
        Site site = new Site("site name", "site link");
        site.setNewsCount(10);
        return site;
    }

    private Site getExampleSiteWithLastUpdate() {
        Site siteWithLastUpdate = new Site("site name", "site link");
        siteWithLastUpdate.setLastUpdate(LocalDateTime.of(1999, 1, 1, 0, 0));
        siteWithLastUpdate.setNewsCount(10);
        siteWithLastUpdate.setAvgUpdateTime(1);
        return siteWithLastUpdate;
    }

    @Test
    public void getNewPublicationDates() {
        Site site = getExampleSite();

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

        ScheduleUpdater scheduleUpdater = new ScheduleUpdater(scheduledService, rssService, site, 5, 0);
        List<LocalDateTime> newPublicationDates = scheduleUpdater.getNewPublicationDates();
        assertEquals(dateTimes, newPublicationDates);

        PowerMockito.when(rssService.fetchFromURL(site.getLink())).thenThrow(new SyndFeedException());
        newPublicationDates = scheduleUpdater.getNewPublicationDates();
        assertEquals(0, newPublicationDates.size());
    }

    @Test
    public void getSumOfIntervals() {
        Site site = getExampleSite();
        Site siteWithLastUpdate = getExampleSiteWithLastUpdate();

        List<LocalDateTime> dateTimes = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            LocalDateTime dateTime = LocalDateTime.of(2000, 1, i, 0, 0);
            dateTimes.add(dateTime);
        }

        ScheduleUpdater scheduleUpdater = new ScheduleUpdater(scheduledService, rssService, site, 5, 0);
        long sumOfIntervals = scheduleUpdater.getSumOfIntervals(dateTimes);
        long realSumOfIntervals = 0;
        for (int i = 0; i < dateTimes.size() - 1; i++) {
            realSumOfIntervals += dateTimes.get(i).until(dateTimes.get(i + 1), ChronoUnit.MILLIS);
        }
        assertEquals(realSumOfIntervals, sumOfIntervals);

        scheduleUpdater = new ScheduleUpdater(scheduledService, rssService, siteWithLastUpdate, 5, 0);
        sumOfIntervals = scheduleUpdater.getSumOfIntervals(dateTimes);
        realSumOfIntervals += siteWithLastUpdate.getLastUpdate().until(dateTimes.get(0), ChronoUnit.MILLIS);
        assertEquals(realSumOfIntervals, sumOfIntervals);
    }

    @Test
    public void getNewAverageUpdateTime() {
        Site site = getExampleSite();
        Site siteWithLastUpdate = getExampleSiteWithLastUpdate();

        List<LocalDateTime> dateTimes = new ArrayList<>();
        long sumOfIntervals = 1000000;
        long lastNewsCount = 0;
        long realNewAverageUpdateTime;

        ScheduleUpdater scheduleUpdater = PowerMockito.spy(new ScheduleUpdater(scheduledService, rssService, site, 5, lastNewsCount));
        PowerMockito.when(scheduleUpdater.getSumOfIntervals(dateTimes)).thenReturn(sumOfIntervals);
        long newAverageUpdateTime = scheduleUpdater.getNewAverageUpdateTime(dateTimes);
        realNewAverageUpdateTime = sumOfIntervals / site.getNewsCount();
        assertEquals(realNewAverageUpdateTime / 1000, newAverageUpdateTime);

        scheduleUpdater = PowerMockito.spy(new ScheduleUpdater(scheduledService, rssService, siteWithLastUpdate, 5, lastNewsCount));
        PowerMockito.doReturn(sumOfIntervals).when(scheduleUpdater).getSumOfIntervals(dateTimes);
        newAverageUpdateTime = scheduleUpdater.getNewAverageUpdateTime(dateTimes);
        realNewAverageUpdateTime = (lastNewsCount * siteWithLastUpdate.getAvgUpdateTime() + sumOfIntervals) / siteWithLastUpdate.getNewsCount();
        assertEquals(realNewAverageUpdateTime / 1000, newAverageUpdateTime);

        sumOfIntervals = 1;
        PowerMockito.doReturn(sumOfIntervals).when(scheduleUpdater).getSumOfIntervals(dateTimes);
        newAverageUpdateTime = scheduleUpdater.getNewAverageUpdateTime(dateTimes);
        assertEquals(ScheduleUpdater.DEFAULT_UPDATE_INTERVAL, newAverageUpdateTime);
    }

    @Test
    public void calculateUpdateInterval() {
        Site site = getExampleSite();

        List<LocalDateTime> dateTimes = new ArrayList<>();
        long lastUpdateTime = 5;
        long newAverageUpdateTime = 10;

        ScheduleUpdater scheduleUpdater = PowerMockito.spy(new ScheduleUpdater(scheduledService, rssService, site, lastUpdateTime, 0));
        PowerMockito.doReturn(dateTimes).when(scheduleUpdater).getNewPublicationDates();
        PowerMockito.doReturn(newAverageUpdateTime).when(scheduleUpdater).getNewAverageUpdateTime(dateTimes);

        scheduleUpdater.calculateUpdateInterval();
        assertEquals(lastUpdateTime * 2, scheduleUpdater.getUpdateInterval());

        LocalDateTime nowTime = LocalDateTime.now();
        dateTimes.add(nowTime);
        scheduleUpdater.calculateUpdateInterval();
        assertEquals(newAverageUpdateTime, scheduleUpdater.getUpdateInterval());
        assertEquals(newAverageUpdateTime, site.getAvgUpdateTime());
        assertEquals(nowTime, site.getLastUpdate());

        newAverageUpdateTime = 5 * 3600;
        PowerMockito.doReturn(newAverageUpdateTime).when(scheduleUpdater).getNewAverageUpdateTime(dateTimes);
        scheduleUpdater.calculateUpdateInterval();
        assertEquals(3 * 3600, scheduleUpdater.getUpdateInterval());
    }

    @Test
    public void scheduleUpdaterDefaultInterval() {
        Site site = getExampleSite();
        ScheduleUpdater scheduleUpdater = new ScheduleUpdater(scheduledService, rssService, site, 0, 0);
        assertEquals(ScheduleUpdater.DEFAULT_UPDATE_INTERVAL, scheduleUpdater.getUpdateInterval());
    }
}
