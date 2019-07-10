package in.nimbo;

import in.nimbo.application.App;
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

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest({})
public class AppTest {
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

    @Test
    public void splitArguments() {
        App app = new App(siteDAO, schedule, rssService);
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
}
