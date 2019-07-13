package in.nimbo.application.cli;

import com.rometools.rome.feed.synd.SyndFeedImpl;
import in.nimbo.TestUtility;
import in.nimbo.dao.ContentDAO;
import in.nimbo.dao.EntryDAO;
import in.nimbo.dao.SiteDAO;
import in.nimbo.entity.Site;
import in.nimbo.exception.SyndFeedException;
import in.nimbo.service.RSSService;
import in.nimbo.service.schedule.Schedule;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(PowerMockRunner.class)
public class AddCLITest {
    private SiteDAO siteDAO;
    private ContentDAO contentDAO;
    private EntryDAO entryDAO;
    private RSSService rssService;
    private Schedule schedule;

    @BeforeClass
    public static void init() {
        TestUtility.disableJOOQLogo();
    }

    @Before
    public void beforeEachTest() {
        siteDAO = PowerMockito.mock(SiteDAO.class);
        contentDAO = PowerMockito.mock(ContentDAO.class);
        entryDAO = PowerMockito.mock(EntryDAO.class);
        schedule = PowerMockito.mock(Schedule.class);
        rssService = PowerMockito.spy(new RSSService(entryDAO, siteDAO, contentDAO));
    }

    private List<Site> createExampleSites() {
        List<Site> sites = new ArrayList<>();
        sites.add(new Site("site 1", "link 1"));
        sites.add(new Site("site 2", "link 2"));
        sites.add(new Site("site 3", "link 3"));
        return sites;
    }

    @Test(expected = IllegalArgumentException.class)
    public void addDuplicateSite() {
        PowerMockito.when(siteDAO.containLink("link 1")).thenReturn(true);
        PowerMockito.when(siteDAO.getSites()).thenReturn(createExampleSites());
        PowerMockito.doReturn(new SyndFeedImpl()).when(rssService).fetchFeedFromURL(Matchers.anyString());
        AddCLI addCLI = PowerMockito.spy(new AddCLI());
        addCLI.addSite(schedule, rssService, "site 1", "link 1");
    }

    @Test
    public void addNonDuplicateSite() {
        PowerMockito.when(siteDAO.containLink("link 4")).thenReturn(false);
        PowerMockito.when(siteDAO.getSites()).thenReturn(createExampleSites());
        PowerMockito.doReturn(new SyndFeedImpl()).when(rssService).fetchFeedFromURL(Matchers.anyString());
        AddCLI addCLI = PowerMockito.spy(new AddCLI());
        try {
            addCLI.addSite(schedule, rssService, "site 4", "link 4");
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void addInvalidSiteURL() {
        PowerMockito.when(siteDAO.containLink("link 4")).thenReturn(false);
        PowerMockito.when(siteDAO.getSites()).thenReturn(createExampleSites());
        PowerMockito.doThrow(new SyndFeedException()).when(rssService).fetchFeedFromURL(Matchers.anyString());
        AddCLI addCLI = PowerMockito.spy(new AddCLI());
        try {
            addCLI.addSite(schedule, rssService, "site 4", "link 4");
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SyndFeedException);
        }
    }
}
