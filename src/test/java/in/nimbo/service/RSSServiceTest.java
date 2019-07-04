package in.nimbo.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.FeedException;
import in.nimbo.dao.EntryDAO;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import in.nimbo.entity.Entry;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RSSServiceTest {
    private static EntryDAO dao;
    private static RSSService service;
    @BeforeClass
    public static void init(){
        dao = mock(EntryDAO.class);
        service = new RSSService(dao);
    }
    @Test
    public void save() {
        SyndEntry syndEntry = new SyndEntryImpl();
        syndEntry.setPublishedDate(new Date());

        SyndFeed feed = new SyndFeedImpl();
        Entry entry = new Entry();
        entry.setSyndEntry(syndEntry);
        entry.setChannel("test");

        List<SyndEntry> entries = new ArrayList<>();
        entries.add(entry.getSyndEntry());
        feed.setEntries(entries);

        when(dao.save(entry)).thenReturn(entry);
        when(dao.contain(entry)).thenReturn(false);

        List<Entry> savedEntries = service.save(feed);
        assertEquals(savedEntries.size(), feed.getEntries().size());
    }
    @Test
    public void getFeeds(){
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry());
        entries.add(new Entry());
        entries.add(new Entry());
        when(dao.getEntries()).thenReturn(entries);
//        List<Entry> feeds = service.filterEntryByTitle();
//        assertEquals(feeds, entries);
    }

    @Test
    public void searchFeeds(){
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry());
        entries.add(new Entry());
        entries.add(new Entry());
        when(dao.filterEntryByTitle(null, "نود", null, null)).thenReturn(entries);
        List<Entry> feeds = service.filterEntryByTitle(null, "نود", null, null);
        assertEquals(feeds, entries);
    }
}
