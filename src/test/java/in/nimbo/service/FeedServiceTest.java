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
import java.util.List;

public class FeedServiceTest {
    @Test
    public void save() throws IOException, FeedException {
        FeedDAO dao = mock(FeedDAO.class);
        SyndEntry entry = mock(SyndEntry.class);
        when(dao.save(entry)).thenReturn(entry);
        FeedService service = new FeedService(dao);
        List<SyndEntry> save = service.save("https://90tv.ir/rss/news");
        for (SyndEntry syndEntry:save) {
            assertNull(syndEntry);
        }
    }
}
