package in.nimbo;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.io.FeedException;
import in.nimbo.dao.FeedDAO;
import in.nimbo.service.FeedService;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    private static App app;
    private static FeedService service;
    @BeforeClass
    public static void init() throws FileNotFoundException {
        service = mock(FeedService.class);
        app = new App(service, new Scanner(new FileInputStream("input.txt")));
        List<SyndEntry> entries = new ArrayList<>();
        SyndEntry entry = new SyndEntryImpl();
        entry.setTitle("تست");
        entries.add(entry);
        when(service.getFeeds()).thenReturn(entries);
    }

    @Test
    public void shouldAnswerWithTrue() throws IOException, FeedException {
        app.run();
    }
}
