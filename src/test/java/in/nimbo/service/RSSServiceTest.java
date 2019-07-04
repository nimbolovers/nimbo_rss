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
import org.powermock.api.mockito.PowerMockito;

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
        service = spy(new RSSService(dao));
    }
    @Test
    public void save() throws IOException, FeedException {
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
        doReturn("content").when(service).getContentOfRSSLink(syndEntry.getLink());

        List<Entry> savedEntries = service.save(feed);
        assertEquals(savedEntries.size(), feed.getEntries().size());
        when(dao.contain(entry)).thenReturn(true);
        savedEntries = service.save(feed);
        assertEquals(savedEntries.size(), 0);
    }
    @Test
    public void getFeeds(){
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry());
        entries.add(new Entry());
        entries.add(new Entry());
        when(dao.getEntries()).thenReturn(entries);
        List<Entry> feeds = service.filterEntryByTitle();
        assertEquals(feeds, entries);
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

    @Test
    public void fetchFromUrl(){
        try {
            service.fetchFromURL("https://google.com");
            fail();
        }catch (RuntimeException e){
        }
        try {
            service.fetchFromURL("a");
            fail();
        }catch (RuntimeException e){
        }
        service.fetchFromURL("https://www.tabnak.ir/fa/rss/1");
    }

    @Test
    public void getContent(){
        String content = service.getContentOfRSSLink("https://www.tabnak.ir/fa/news/909490/%D8%A2%D8%BA%D8%A7%D8%B2-%D8%A8%D9%87-%DA%A9%D8%A7%D8%B1-%D8%A7%D9%88%D9%84%DB%8C%D9%86-%D8%B3%D9%81%DB%8C%D8%B1-%D8%B2%D9%86-%D8%B9%D8%B1%D8%A8%D8%B3%D8%AA%D8%A7%D9%86-%D8%AF%D8%B1-%D8%A2%D9%85%D8%B1%DB%8C%DA%A9%D8%A7");
        assertTrue(!content.isEmpty());
    }
}
