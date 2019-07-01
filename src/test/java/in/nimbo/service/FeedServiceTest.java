package in.nimbo.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.io.FeedException;
import in.nimbo.dao.FeedDAO;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FeedServiceTest {
    private static FeedDAO dao;
    private static SyndEntry entry;
    private static FeedService service;
    @BeforeClass
    public static void init(){
        dao = mock(FeedDAO.class);
        entry = mock(SyndEntry.class);
        service = new FeedService(dao);
    }
    @Test
    public void save() throws IOException, FeedException {
        when(dao.save(entry)).thenReturn(entry);
        List<SyndEntry> save = service.save("https://90tv.ir/rss/news");
        for (SyndEntry syndEntry:save) {
            assertNull(syndEntry);
        }
    }
    @Test
    public void getFeeds(){
        List<SyndEntry> entries = new ArrayList<>();
        entries.add(new SyndEntryImpl());
        entries.add(new SyndEntryImpl());
        entries.add(new SyndEntryImpl());
        when(dao.getFeeds()).thenReturn(entries);
        List<SyndEntry> feeds = service.getFeeds();
        assertEquals(feeds, entries);
    }

    @Test
    public void searchFeeds(){
        List<SyndEntry> entries = new ArrayList<>();
        entries.add(new SyndEntryImpl());
        entries.add(new SyndEntryImpl());
        entries.add(new SyndEntryImpl());
        when(dao.getFeeds("نود")).thenReturn(entries);
        List<SyndEntry> feeds = service.getFeeds("نود");
        assertEquals(feeds, entries);
    }
}
