package in.nimbo.dao;

import in.nimbo.TestUtility;
import in.nimbo.dao.pool.ConnectionPool;
import in.nimbo.dao.pool.ConnectionWrapper;
import in.nimbo.entity.Entry;
import in.nimbo.exception.RecordNotFoundException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ConnectionPool.class)
public class EntryDAOTest {
    private static ConnectionWrapper connection;
    private static EntryDAO entryDAO;

    @BeforeClass
    public static void init() throws SQLException {
        TestUtility.disableJOOQLogo();

        DescriptionDAO descriptionDAO = new DescriptionDAOImpl();
        ContentDAO contentDAO = new ContentDAOImpl();
        entryDAO = new EntryDAOImpl(descriptionDAO, contentDAO);

        String initialH2Query = TestUtility.getFileContent(Paths.get("db/db_tables_sql.sql"));
        connection = new ConnectionWrapper(DriverManager.getConnection("jdbc:h2:mem:test/h2_rss", "user", ""));
        connection.prepareStatement(initialH2Query).executeUpdate();
        connection = PowerMockito.spy(connection);
    }

    @Before
    public void initBeforeEachTest() throws SQLException {
        PowerMockito.mockStatic(ConnectionPool.class);
        PowerMockito.when(ConnectionPool.getConnection()).thenReturn(connection);
        PowerMockito.doNothing().when(connection).close();

        connection.prepareStatement("DELETE FROM content;" +
                "DELETE FROM description;" +
                "DELETE FROM feed").executeUpdate();
    }

    private List<Entry> createExampleEntries1() {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Entry entry = TestUtility.createEntry("channel-" + i, "title-" + i, "link-" + i,
                    (i & 1) != 0 ? new Date() : null,
                    "content-" + i,
                    (i & 2) != 0 ? "desc-" + i : null);
            entries.add(entry);
        }
        return entries;
    }

    @Test
    public void save() throws SQLException {
        List<Entry> entries = createExampleEntries1();
        for (Entry entry : entries) {
            entryDAO.save(entry);
        }

        PreparedStatement statement = connection.prepareStatement(
                "SELECT feed.*, content.value as cnt FROM feed " +
                        "INNER JOIN content ON feed.id=content.feed_id");
        ResultSet resultSet = statement.executeQuery();
        List<Entry> fetchedEntries = new ArrayList<>();
        while (resultSet.next()) {
            Entry e = TestUtility.createEntry(resultSet.getString("channel"), resultSet.getString("title"),
                    resultSet.getString("link"), resultSet.getDate("pub_date"),
                    resultSet.getString("cnt"), "");
            e.setId(resultSet.getInt("id"));
            fetchedEntries.add(e);
        }

        assertEquals(entries, fetchedEntries);
    }

    @Test
    public void getEntries() {
        List<Entry> entries = createExampleEntries1();
        for (Entry entry : entries) {
            entryDAO.save(entry);
        }

        assertEquals(entries, entryDAO.getEntries());
    }

    @Test(expected = RecordNotFoundException.class)
    public void getEntriesWithoutContent() throws SQLException {
        connection.prepareStatement("INSERT INTO feed(channel, title, link) VALUES('channel', 'title', 'link')").executeUpdate();
        entryDAO.getEntries();
    }

    private List<Entry> createExampleEntries2() {
        List<Entry> entries = new ArrayList<>();
        entries.add(TestUtility.createEntry("channel 1", "title 1", "2010", TestUtility.getDate(2010, 1, 1), "content 1", "desc 1"));
        entries.add(TestUtility.createEntry("channel 2", "title 2", "2020", TestUtility.getDate(2020, 1, 1), "content 2", "desc 2"));
        return entries;
    }

    @Test
    public void filterEntryByTitle() {
        List<Entry> entries = createExampleEntries2();
        for (Entry entry : entries) {
            entryDAO.save(entry);
        }

        // test before
        Date beforeDate = TestUtility.getDate(2000, 1, 1);
        assertEquals(entries.stream().filter(entry -> entry.getSyndEntry().getPublishedDate()
                        .compareTo(beforeDate) >= 0).collect(Collectors.toList()),
                entryDAO.filterEntryByTitle(null, "title", beforeDate, null));
        // test after
        Date afterDate = TestUtility.getDate(2030, 1, 1);
        assertEquals(entries.stream().filter(entry -> entry.getSyndEntry().getPublishedDate()
                        .compareTo(beforeDate) >= 0).collect(Collectors.toList()),
                entryDAO.filterEntryByTitle(null, "title", null, afterDate));
        // test between
        assertEquals(entries.stream().filter(entry -> entry.getSyndEntry().getPublishedDate()
                        .compareTo(beforeDate) >= 0).collect(Collectors.toList()),
                entryDAO.filterEntryByTitle(null, "title", beforeDate, afterDate));
    }

    @Test
    public void filterEntryByContent() {
        List<Entry> entries = createExampleEntries2();
        for (Entry entry : entries) {
            entryDAO.save(entry);
        }

        // test before
        Date beforeDate = TestUtility.getDate(2000, 1, 1);
        assertEquals(entries.stream().filter(entry -> entry.getSyndEntry().getPublishedDate()
                        .compareTo(beforeDate) >= 0).collect(Collectors.toList()),
                entryDAO.filterEntryByContent(null, "content", beforeDate, null));
        // test after
        Date afterDate = TestUtility.getDate(2030, 1, 1);
        assertEquals(entries.stream().filter(entry -> entry.getSyndEntry().getPublishedDate()
                        .compareTo(beforeDate) >= 0).collect(Collectors.toList()),
                entryDAO.filterEntryByContent(null, "content", null, afterDate));
        // test between
        assertEquals(entries.stream().filter(entry -> entry.getSyndEntry().getPublishedDate()
                        .compareTo(beforeDate) >= 0).collect(Collectors.toList()),
                entryDAO.filterEntryByContent(null, "content", beforeDate, afterDate));
    }

    @Test
    public void contain() {
        List<Entry> entries = new ArrayList<>();
        entries.add(TestUtility.createEntry("channel 1", "title 1", "link 1", new Date(), "content 1", "desc 1"));
        entries.add(TestUtility.createEntry("channel 2", "title 2", "link 2", new Date(), "content 2", "desc 2"));
        for (Entry entry : entries) {
            entryDAO.save(entry);
        }

        assertTrue(entryDAO.contain(TestUtility.createEntry("channel-test", "title-test", "link 1", new Date(), "content-test", "desc-test")));
        assertTrue(entryDAO.contain(TestUtility.createEntry("channel-test", "title-test", "link 2", new Date(), "content-test", "desc-test")));
        assertFalse(entryDAO.contain(TestUtility.createEntry("channel-test", "title-test", "link 3", new Date(), "content-test", "desc-test")));
    }
}
