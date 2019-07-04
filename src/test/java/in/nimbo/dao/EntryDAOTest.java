package in.nimbo.dao;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import in.nimbo.entity.Entry;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Random;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DAO.class)
public class EntryDAOTest {
    private static DescriptionDAO descriptionDAO;
    private static ContentDAO contentDAO;
    private static Connection connection;
    private static EntryDAO entryDAO;

    static {
        // disable logs of JOOQ for query
        System.getProperties().setProperty("org.jooq.no-logo", "true");
    }

    private static String readFile(String path) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(path));
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("couldn't read file: " + path, e);
        }
    }

    @BeforeClass
    public static void init() throws SQLException, FileNotFoundException {
        connection = DriverManager.getConnection("jdbc:h2:~/h2_rss", "user", "");
        String queries = readFile("db/db_tables_sql.sql");
        PreparedStatement statement = connection.prepareStatement(queries);
        statement.execute();
        descriptionDAO = new DescriptionDAOImpl();
        contentDAO = new ContentDAOImpl();
        entryDAO = new EntryDAOImpl(descriptionDAO, contentDAO);
    }

    @Before
    public void initTables() throws SQLException {
        PowerMockito.mockStatic(DAO.class);
        PowerMockito.when(DAO.getConnection()).thenReturn(connection);
        PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM content; " +
                "DELETE FROM description;" +
                "DELETE FROM feed");
        statement.executeUpdate();
    }

    private Entry createEntry(String title,
                              Date pubDate,
                              String content,
                              String description,
                              String link,
                              String channel){
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

    @Test
    public void save() throws SQLException {
        List<Entry> entryList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Entry entry = createEntry("title " + i,
                    new Date(),
                    (i & 1) != 0 ? "content " + i : "",
                    (i & 2) != 0 ? "desc" + i : "",
                    "link" + i, "test" + i);
            entryDAO.save(entry);
            entryList.add(entry);
        }
        PreparedStatement statement = connection.prepareStatement(
                "SELECT feed.*," +
                        " content.value as cnt," +
                        " description.value as des" +
                        " FROM feed " +
                "INNER JOIN content ON feed.id=content.feed_id " +
                "INNER JOIN description ON feed.id=description.feed_id");
        ResultSet resultSet = statement.executeQuery();
        List<Entry> list = new ArrayList<>();
        while (resultSet.next()){
            Entry e = createEntry(resultSet.getString("title"),
                    resultSet.getDate("pub_date"),
                    resultSet.getString("cnt"),
                    resultSet.getString("des"),
                    resultSet.getString("link"),
                    resultSet.getString("channel"));
            e.setId(resultSet.getInt("id"));
            list.add(e);
        }
        assertArrayEquals(entryList.toArray(), list.toArray());
    }

    private List<Entry> saveForFilter(){
        Date date2010 = getDate(2010, 1, 1);
        Date date2020 = getDate(2020, 1, 1);
        Date date2030 = getDate(2030, 1, 1);
        Entry entry2010 = createEntry("title 1", date2010, "test", "desc", "2010", "test");
        Entry entry2020 = createEntry("title 2", date2020, "test", "desc", "2020", "test");
        Entry entry2030 = createEntry("title 3", date2030, "test", "desc", "2030", "test");
        entryDAO.save(entry2010);
        entryDAO.save(entry2020);
        entryDAO.save(entry2030);
        List<Entry> entries = new ArrayList<>();
        entries.add(entry2010);
        entries.add(entry2020);
        entries.add(entry2030);
        return entries;
    }

    @Test
    public void getAllTest(){
        List<Entry> entries = saveForFilter();
        assertArrayEquals(entries.toArray(), entryDAO.getEntries().toArray());
    }

    @Test
    public void filterBeforeTest(){
        List<Entry> entries = saveForFilter();
        entries.remove(0);
        Date date2015 = getDate(2015, 1, 1);
        assertArrayEquals(entries.toArray(), entryDAO.filterEntryByTitle("test", "", date2015, null).toArray());
    }

    @Test
    public void filterAfterTest(){
        List<Entry> entries = saveForFilter();
        entries.remove(2);
        Date date2025 = getDate(2025, 1, 1);
        assertArrayEquals(entries.toArray(), entryDAO.filterEntryByTitle(null, "", null, date2025).toArray());
    }

    @Test
    public void contentTest(){
        List<Entry> entries = saveForFilter();
        entries.remove(2);
        entries.remove(0);

        Date date2025 = getDate(2025, 1, 1);
        Date date2015 = getDate(2015, 1, 1);
        assertArrayEquals(entries.toArray(), entryDAO.filterEntryByContent("test", "", date2015, date2025).toArray());
    }

    private Date getDate(int year, int month, int day){
        LocalDateTime dateTime = LocalDateTime.of(LocalDate.of(year, month, day), LocalTime.of(0, 0));
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

}
