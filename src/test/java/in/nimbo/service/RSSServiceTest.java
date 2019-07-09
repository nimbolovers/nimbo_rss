package in.nimbo.service;

import com.rometools.rome.feed.synd.*;
import in.nimbo.TestUtility;
import in.nimbo.dao.EntryDAO;
import in.nimbo.dao.SiteDAO;
import in.nimbo.entity.Entry;
import in.nimbo.entity.Site;
import in.nimbo.entity.report.DateReport;
import in.nimbo.entity.report.HourReport;
import in.nimbo.exception.ContentExtractingException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({EntryDAO.class, Entry.class})
public class RSSServiceTest {
    private static EntryDAO entryDAO;
    private static RSSService rssService;
    private static SiteDAO siteDAO;

    @BeforeClass
    public static void init() {
        TestUtility.disableJOOQLogo();
    }

    @Before
    public void beforeAnyTest() {
        entryDAO = PowerMockito.mock(EntryDAO.class);
        siteDAO = PowerMockito.mock(SiteDAO.class);
        rssService = spy(new RSSService(entryDAO, siteDAO));
    }

    @Test
    public void addSiteEntriesWithoutContain() {
        Entry entry1 = TestUtility.createEntry("channel", "title", "link", LocalDateTime.now(), "content", "description");
        Site site = new Site("site-name", "site-link");
        List<Entry> entries = new ArrayList<>();
        entries.add(entry1);
        when(entryDAO.save(entry1)).thenReturn(entry1);
        when(entryDAO.contain(Matchers.any(Entry.class))).thenReturn(false);
        doReturn("content").when(rssService).getContentOfRSSLink(entry1.getLink());

        List<Entry> savedEntries = rssService.addSiteEntries(site.getLink(), entries);
        assertEquals(entries.size(), savedEntries.size());
    }

    @Test
    public void addSiteEntriesWithContain() {
        Entry entry1 = TestUtility.createEntry("channel 1", "title 1", "link 1", LocalDateTime.now(), "content 1", "description 1");
        Entry entry2 = TestUtility.createEntry("channel 2", "title 2", "link 2", LocalDateTime.now(), "content 2", "description 2");
        Site site = new Site("site-name", "site-link");
        List<Entry> entries = new ArrayList<>();
        entries.add(entry1);
        entries.add(entry2);
        when(entryDAO.save(entry1)).thenReturn(entry1);
        when(entryDAO.contain(entry1)).thenReturn(false);
        when(entryDAO.contain(entry2)).thenReturn(true);
        doReturn("content").when(rssService).getContentOfRSSLink(entry1.getLink());

        List<Entry> savedEntries = rssService.addSiteEntries(site.getLink(), entries);
        assertEquals(1, savedEntries.size());
    }

    @Test
    public void addSiteEntriesWithNoContent() {
        Entry entry = TestUtility.createEntry("channel", "title", "link", LocalDateTime.now(), "content", "description");
        Site site = new Site("site-name", "site-link");
        List<Entry> entries = new ArrayList<>();
        entries.add(entry);

        when(entryDAO.save(entry)).thenReturn(entry);
        doThrow(new ContentExtractingException()).when(rssService).getContentOfRSSLink(entry.getLink());

        List<Entry> savedEntries = rssService.addSiteEntries(site.getLink(), entries);
        assertEquals(savedEntries.size(), entries.size());
    }

    @Test
    public void getEntries() {
        List<Entry> entries = new ArrayList<>();
        entries.add(TestUtility.createEntry("channel", "title 1", "link 1", LocalDateTime.now(), null, "desc 1"));
        entries.add(TestUtility.createEntry("channel", "title 2", "link 2", LocalDateTime.now(), null, "desc 2"));
        entries.add(TestUtility.createEntry("channel", "title 3", "link 3", LocalDateTime.now(), null, "desc 3"));

        SyndFeed syndFeed = new SyndFeedImpl();
        syndFeed.setTitle("channel");
        List<SyndEntry> syndEntries = new ArrayList<>();
        syndFeed.setEntries(syndEntries);
        for (Entry entry : entries) {
            SyndEntry syndEntry = new SyndEntryImpl();
            syndEntry.setTitle(entry.getTitle());
            syndEntry.setLink(entry.getLink());
            syndEntry.setPublishedDate(Date.from(entry.getPublicationDate().atZone(ZoneId.systemDefault()).toInstant()));

            SyndContent syndContent = new SyndContentImpl();
            syndContent.setType(entry.getDescription().getType());
            syndContent.setMode(entry.getDescription().getMode());
            syndContent.setValue(entry.getDescription().getValue());
            syndEntry.setDescription(syndContent);

            syndEntries.add(syndEntry);
        }

        assertEquals(entries, rssService.getEntries(syndFeed));
    }

    @Test
    public void getDateReportsTest(){
        when(siteDAO.getCount()).thenReturn(2);
        List<DateReport> reports = new ArrayList<>();
        int limit = 6;
        for (int i = 0; i < limit; i++) {
            DateReport report = new DateReport("test" , i + 1, LocalDateTime.now());
            reports.add(report);
        }
        when(entryDAO.getDateReports("test", limit)).thenReturn(reports);
        List<DateReport> test = rssService.getReports("test");
        assertEquals(test, reports);
    }

    @Test
    public void getHourReportTest(){
        List<HourReport> reports = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            reports.add(new HourReport("channel", i + 1, i));
        }
        when(entryDAO.getHourReports("test")).thenReturn(reports);

        assertEquals(reports, rssService.getHourReports("test"));
    }
}
