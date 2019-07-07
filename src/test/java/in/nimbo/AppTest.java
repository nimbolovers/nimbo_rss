package in.nimbo;

import in.nimbo.application.App;
import in.nimbo.application.Utility;
import in.nimbo.dao.SiteDAO;
import in.nimbo.entity.Site;
import in.nimbo.service.RSSService;
import in.nimbo.service.schedule.Schedule;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.MalformedParametersException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({})
public class AppTest {
    private SiteDAO siteDAO;
    private RSSService rssService;
    private Schedule schedule;
    private App app;

    @BeforeClass
    public static void init() {
        TestUtility.disableJOOQLogo();
    }

    @Before
    public void beforeEachTest() {
        siteDAO = PowerMockito.mock(SiteDAO.class);
        rssService = PowerMockito.mock(RSSService.class);
        schedule = PowerMockito.mock(Schedule.class);
        app = new App(siteDAO, schedule, rssService);
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
        List<Site> sites = createExampleSites();
        PowerMockito.when(schedule.getSites()).thenReturn(sites);
        app.addSite("site 1", "link 1");
    }

    @Test
    public void addNonDuplicateSite() {
        List<Site> sites = createExampleSites();
        PowerMockito.when(schedule.getSites()).thenReturn(sites);
        app.addSite("site 4", "link 4");
        assertEquals(4, sites.size());
        assertTrue(sites.contains(new Site("site 4", "link 4")));
    }

    @Test
    public void encodeURL() throws MalformedURLException {
        String link = "link";
        try {
            Utility.encodeURL(link);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof MalformedURLException);
        }

        link = "http://winphone.ir/";
        assertEquals(link, Utility.encodeURL(link).toString());
        link = "http://example.com/سلام";
        assertEquals("http://example.com/%D8%B3%D9%84%D8%A7%D9%85", link = Utility.encodeURL(link).toString());
        assertEquals("http://example.com/%D8%B3%D9%84%D8%A7%D9%85", Utility.encodeURL(link).toString());
    }
}
