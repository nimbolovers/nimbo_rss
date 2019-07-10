package in.nimbo.cli;

import in.nimbo.TestUtility;
import in.nimbo.application.cli.AddCLI;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;

@RunWith(PowerMockRunner.class)
@PrepareForTest({})
public class AddCLITest {
    private SiteDAO siteDAO;
    private RSSService rssService;
    private Schedule schedule;

    @BeforeClass
    public static void init() {
        TestUtility.disableJOOQLogo();
    }

    @Before
    public void beforeEachTest() {
        siteDAO = PowerMockito.mock(SiteDAO.class);
        rssService = PowerMockito.mock(RSSService.class);
        schedule = PowerMockito.mock(Schedule.class);
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
        AddCLI addCLI = PowerMockito.spy(new AddCLI());
        addCLI.addSite(schedule, siteDAO, "site 1", "link 1");
    }

    @Test
    public void addNonDuplicateSite() {
        PowerMockito.when(siteDAO.containLink("link 4")).thenReturn(false);
        PowerMockito.when(siteDAO.getSites()).thenReturn(createExampleSites());
        AddCLI addCLI = PowerMockito.spy(new AddCLI());
        try {
            addCLI.addSite(schedule, siteDAO, "site 4", "link 4");
        } catch (Exception e) {
            fail();
        }
    }
}
