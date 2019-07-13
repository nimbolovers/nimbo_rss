package in.nimbo.application.cli;

import in.nimbo.TestUtility;
import in.nimbo.application.Utility;
import in.nimbo.entity.Entry;
import in.nimbo.exception.RssServiceException;
import in.nimbo.service.RSSService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class SearchCLITest {
    private RSSService rssService;
    private SearchCLI searchCLI;

    @BeforeClass
    public static void init() {
        TestUtility.disableJOOQLogo();
    }

    @Before
    public void beforeEachTest() {
        rssService = mock(RSSService.class);
        searchCLI = spy(new SearchCLI());
    }

    @Test
    public void showFilteredEntry() throws RssServiceException {
        List<Entry> entries = new ArrayList<>();
        entries.add(TestUtility.createEntry("channel 1", "title 1", "link 1", LocalDateTime.now(), "content 1", "desc 1"));
        entries.add(TestUtility.createEntry("channel 2", "title 2", "link 2", LocalDateTime.now(), "content 2", "desc 2"));
        entries.add(TestUtility.createEntry("channel 3", "title 3", "link 3", LocalDateTime.now(), "content 3", "desc 3"));
        doReturn(entries).when(rssService).filterEntry(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(),
                Matchers.any(LocalDateTime.class), Matchers.any(LocalDateTime.class));
        try {
            searchCLI.filterEntry(rssService, null, null, "channel", "content", "title");
            searchCLI.filterEntry(rssService, Utility.formatter.format(LocalDateTime.now()), Utility.formatter.format(LocalDateTime.now()),
                    "channel", "content", "title");
        } catch (Exception e) {
            fail();
        }

        try {
            doThrow(new RssServiceException()).when(rssService).filterEntry(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(),
                    Matchers.any(LocalDateTime.class), Matchers.any(LocalDateTime.class));
            searchCLI.filterEntry(rssService, null, null, "channel", "content", "title");
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof RssServiceException);
        }
    }

    @Test(expected = RssServiceException.class)
    public void showFilteredEntryWithException1() throws RssServiceException {
        doThrow(new RssServiceException()).when(rssService).filterEntry(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(),
                Matchers.any(LocalDateTime.class), Matchers.any(LocalDateTime.class));
        searchCLI.filterEntry(rssService, null, null, "channel", "content", "title");
    }

    @Test(expected = IllegalArgumentException.class)
    public void showFilteredEntryWithException2() throws RssServiceException {
        searchCLI.filterEntry(rssService, "illegal date", "illegal date", "channel", "content", "title");
    }
}
