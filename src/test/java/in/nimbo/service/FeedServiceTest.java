package in.nimbo.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.io.FeedException;
import in.nimbo.dao.FeedDAO;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import in.nimbo.entity.Entry;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FeedServiceTest {
    private static FeedDAO dao;
    private static Entry entry;
    private static FeedService service;
    @BeforeClass
    public static void init(){
        dao = mock(FeedDAO.class);
        entry = mock(Entry.class);
        service = new FeedService(dao);
    }
    @Test
    public void save() throws IOException, FeedException {
        when(dao.save(entry)).thenReturn(entry);
        List<Entry> save = service.save("https://90tv.ir/rss/news");
        for (Entry entry:save) {
            assertNull(entry);
        }
    }
    @Test
    public void getFeeds(){
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry());
        entries.add(new Entry());
        entries.add(new Entry());
        when(dao.getEntries()).thenReturn(entries);
        List<Entry> feeds = service.getFeeds();
        assertEquals(feeds, entries);
    }

    @Test
    public void searchFeeds(){
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry());
        entries.add(new Entry());
        entries.add(new Entry());
        when(dao.getEntryByTitle("نود")).thenReturn(entries);
        List<Entry> feeds = service.getFeeds("نود");
        assertEquals(feeds, entries);
    }
}
