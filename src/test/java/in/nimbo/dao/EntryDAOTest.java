package in.nimbo.dao;

import in.nimbo.TestUtility;
import in.nimbo.dao.pool.ConnectionPool;
import in.nimbo.dao.pool.ConnectionWrapper;
import in.nimbo.entity.Entry;
import in.nimbo.exception.RecordNotFoundException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ConnectionPool.class)
public class EntryDAOTest {
    private static ConnectionWrapper connection;
    private static EntryDAO entryDAO;

    @BeforeClass
    public static void init() throws SQLException, ClassNotFoundException {
        TestUtility.disableJOOQLogo();

        DescriptionDAO descriptionDAO = new DescriptionDAOImpl();
        ContentDAO contentDAO = new ContentDAOImpl();
        entryDAO = new EntryDAOImpl(descriptionDAO, contentDAO);

        String initialH2Query = TestUtility.getFileContent(Paths.get("db/db_tables_sql.sql"));
        Class.forName(TestUtility.getDatabaseProperties().getProperty("database.driver"));
        connection = new ConnectionWrapper(DriverManager.getConnection(
                TestUtility.getDatabaseProperties().getProperty("database.url"),
                TestUtility.getDatabaseProperties().getProperty("database.username"),
                TestUtility.getDatabaseProperties().getProperty("database.password"))
        );

        connection.prepareStatement(initialH2Query).executeUpdate();
        connection = PowerMockito.spy(connection);
    }

    @AfterClass
    public static void finish() throws SQLException {
        if (connection != null)
            connection.close();
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
        entries.add(TestUtility.createEntry("channel", "title 1", "2010", TestUtility.createDate(2010, 1, 1), "content 1", "desc 1"));
        entries.add(TestUtility.createEntry("channel", "title 2", "2020", TestUtility.createDate(2020, 1, 1), "content 2", "desc 2"));
        return entries;
    }

    @Test
    public void filterEntry() {
        List<Entry> entries = createExampleEntries2();
        for (Entry entry : entries) {
            entryDAO.save(entry);
        }

        // test before
        Date beforeDate = TestUtility.createDate(2000, 1, 1);
        Date afterDate = TestUtility.createDate(2030, 1, 1);

        assertEquals(entries.stream()
                        .filter(entry -> entry.getPublicationDate().compareTo(beforeDate) >= 0)
                        .collect(Collectors.toList()),
                entryDAO.filterEntry("channel", "title", "content", beforeDate, null));
        // test after
        assertEquals(entries.stream()
                        .filter(entry -> entry.getPublicationDate().compareTo(beforeDate) >= 0)
                        .filter(entry -> entry.getPublicationDate().compareTo(afterDate) <= 0)
                        .collect(Collectors.toList()),
                entryDAO.filterEntry("channel", "title", "content", beforeDate, afterDate));
        // test between
        assertEquals(entries.stream()
                        .filter(entry -> entry.getPublicationDate().compareTo(afterDate) <= 0)
                        .collect(Collectors.toList()),
                entryDAO.filterEntry("channel", "title", "content", null, afterDate));
    }

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
