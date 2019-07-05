package in.nimbo;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import in.nimbo.entity.Entry;

import java.util.Date;

public class TestUtility {
    private TestUtility() {
    }

    /**
     * disable JOOQ library logger
     */
    public static void disableJOOQLogo() {
        System.getProperties().setProperty("org.jooq.no-logo", "true");
    }

    /**
     * create an entity for test purpose
     * @param channel channel
     * @param title title
     * @param link link
     * @param pubDate publication date
     * @param content content
     * @param description description
     * @return entry which is created
     */
    public static Entry createEntry(String channel, String title,
                                    String link, Date pubDate,
                                    String content,
                                    String description) {
        Entry entry = new Entry();
        SyndEntry syndEntry = new SyndEntryImpl();
        SyndContent desc = null;

        if (description != null) {
            desc = new SyndContentImpl();
            desc.setType("text/html");
            desc.setValue(description);
        }

        syndEntry.setTitle(title);
        syndEntry.setPublishedDate(pubDate);
        syndEntry.setLink(link);
        syndEntry.setDescription(desc);

        entry.setChannel(channel);
        entry.setSyndEntry(syndEntry);
        entry.setContent(content);
        return entry;
    }
}
