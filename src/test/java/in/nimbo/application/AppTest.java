package in.nimbo.application;

import in.nimbo.TestUtility;
import in.nimbo.dao.SiteDAO;
import in.nimbo.entity.Site;
import in.nimbo.service.RSSService;
import in.nimbo.service.schedule.Schedule;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

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
        siteDAO = mock(SiteDAO.class);
        rssService = mock(RSSService.class);
        schedule = mock(Schedule.class);
        app = new App(schedule, rssService);
    }

    @Test
    public void splitArguments() {
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            strings.add(String.valueOf(i));
        }
        assertEquals(strings, app.splitArguments(String.join(" ", strings)));

        strings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            strings.add('"' + String.valueOf(i) + '"');
        }
        assertEquals(strings, app.splitArguments(String.join(" ", strings)));
    }

    @Test
    public void appInit() {
        List<Site> sites = new ArrayList<>();
        sites.add(new Site("site 1", "link 1"));
        sites.add(new Site("site 2", "link 2"));
        doNothing().when(schedule).scheduleSite(Matchers.any(Site.class));
        doNothing().when(schedule).scheduleSiteDAO(sites);
        when(rssService.getSiteDAO()).thenReturn(siteDAO);
        doReturn(sites).when(siteDAO).getSites();
        try {
            app.doSchedule();
            app.init();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
