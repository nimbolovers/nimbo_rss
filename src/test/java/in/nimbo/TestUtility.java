package in.nimbo;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import in.nimbo.entity.Description;
import in.nimbo.entity.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Properties;

/**
 * Utility class for do same work between tests
 */
public class TestUtility {
    private static Properties databaseProp;
    private static Logger logger = LoggerFactory.getLogger(TestUtility.class);

    private TestUtility() {
    }

    /**
     * disable JOOQ library logger
     */
    public static void disableJOOQLogo() {
        System.getProperties().setProperty("org.jooq.no-logo", "true");
    }

    /**
     *
     * @return property for get properties of database
     */
    public static Properties getDatabaseProperties() {
        if (databaseProp != null)
            return databaseProp;
        try {
            ClassLoader loader = TestUtility.class.getClassLoader();
            databaseProp = new Properties();
            InputStream is = loader.getResourceAsStream("database.properties");
            databaseProp.load(is);
            return databaseProp;
        } catch (IOException e) {
            logger.error("Unable to load database properties", e);
            throw new RuntimeException("Unable to load database properties", e);
        }
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
        Description desc = null;

        if (description != null) {
            desc = new Description();
            desc.setType("text/html");
            desc.setValue(description);
        }

        entry.setDescription(desc);
        entry.setChannel(channel);
        entry.setContent(content);
        entry.setPublicationDate(pubDate);
        entry.setLink(link);
        entry.setTitle(title);
        return entry;
    }

    /**
     * get content a file as string
     * @param path path of file
     * @return content of file
     * @throws RuntimeException if unable to get content of file
     */
    public static String getFileContent(Path path) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Couldn't read file: " + path, e);
            throw new RuntimeException("Couldn't read file: " + path, e);
        }
    }

    /**
     * create java.util.date with given input
     * @param year year
     * @param month month
     * @param day day
     * @return date with given inputs
     */
    public static Date createDate(int year, int month, int day) {
        LocalDateTime dateTime = LocalDateTime.of(LocalDate.of(year, month, day), LocalTime.of(0, 0));
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
