package in.nimbo.service;

import com.rometools.rome.feed.synd.*;
import in.nimbo.TestUtility;
import in.nimbo.dao.EntryDAO;
import in.nimbo.entity.Entry;
import in.nimbo.entity.Site;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({EntryDAO.class, Entry.class})
public class RSSServiceTest {
    private static EntryDAO entryDAO;
    private static RSSService service;

    @BeforeClass
    public static void init() {
        TestUtility.disableJOOQLogo();
        entryDAO = PowerMockito.mock(EntryDAO.class);
        service = spy(new RSSService(entryDAO));
    }

    @Test
    public void addSiteEntries() {
        Entry entry = TestUtility.createEntry("channel", "title", "link", new Date(), "content", "description");
        Site site = new Site("site-name", "site-link");
        List<Entry> entries = new ArrayList<>();
        entries.add(entry);

        when(entryDAO.save(entry)).thenReturn(entry);
        doReturn("content").when(service).getContentOfRSSLink(entry.getLink());

        List<Entry> savedEntries = service.addSiteEntries(site, entries);
        assertEquals(savedEntries.size(), entries.size());
    }
}
