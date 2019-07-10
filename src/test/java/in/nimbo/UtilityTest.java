package in.nimbo;

import in.nimbo.application.Utility;
import in.nimbo.dao.SiteDAO;
import in.nimbo.service.RSSService;
import in.nimbo.service.schedule.Schedule;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.MalformedURLException;
import java.time.LocalDateTime;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({})
public class UtilityTest {
    @BeforeClass
    public static void init() {
        TestUtility.disableJOOQLogo();
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

    @Test
    public void getDate() {
        String date = "illegal";
        try {
            Utility.getDate(date);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }

        date = "01/02/1900 12:01:01";
        LocalDateTime d = Utility.getDate(date);
        assertEquals(date, Utility.formatter.format(d));
    }

    @Test
    public void removeQuotation() {
        assertEquals("salam", Utility.removeQuotation("salam"));
        assertEquals("\"salam", Utility.removeQuotation("\"salam"));
        assertEquals("salam", Utility.removeQuotation("\"salam\""));
        assertEquals("", Utility.removeQuotation(""));
        assertNull(Utility.removeQuotation(null));
    }
}
