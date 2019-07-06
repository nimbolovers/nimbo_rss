package in.nimbo.dao;

import in.nimbo.TestUtility;
import in.nimbo.dao.pool.ConnectionPool;
import in.nimbo.dao.pool.ConnectionWrapper;
import in.nimbo.entity.Entry;
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

import static org.junit.Assert.assertArrayEquals;

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

    @Test
    public void save() throws SQLException {
        List<Entry> entryList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Entry entry = TestUtility.createEntry("channel-" + i, "title-" + i, "link-" + i, new Date(),
                    (i & 1) != 0 ? "content-" + i : "",
                    (i & 2) != 0 ? "desc-" + i : ""
            );
            entryDAO.save(entry);
            entryList.add(entry);
        }
        PreparedStatement statement = connection.prepareStatement(
                "SELECT feed.*, content.value as cnt FROM feed " +
                        "INNER JOIN content ON feed.id=content.feed_id");
        ResultSet resultSet = statement.executeQuery();
        List<Entry> list = new ArrayList<>();
        while (resultSet.next()) {
            Entry e = TestUtility.createEntry(resultSet.getString("channel"), resultSet.getString("title"),
                    resultSet.getString("link"), resultSet.getDate("pub_date"),
                    resultSet.getString("cnt"), "");
            e.setId(resultSet.getInt("id"));
            list.add(e);
        }
        assertArrayEquals(entryList.toArray(), list.toArray());
    }

    private List<Entry> saveForFilter() {
        Entry entry2010 = TestUtility.createEntry("test", "title 1", "2010", TestUtility.getDate(2010, 1, 1), "test", "desc");
        Entry entry2020 = TestUtility.createEntry("test", "title 2", "2020", TestUtility.getDate(2020, 1, 1), "test", "desc");
        Entry entry2030 = TestUtility.createEntry("test", "title 3", "2030", TestUtility.getDate(2030, 1, 1), "test", "desc");
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
    public void getAll() {
        List<Entry> entries = saveForFilter();
        assertArrayEquals(entries.toArray(), entryDAO.getEntries().toArray());
    }

    @Test
    public void filterBeforeTest() {
        List<Entry> entries = saveForFilter();
        entries.remove(0);
        Date date2015 = TestUtility.getDate(2015, 1, 1);
        assertArrayEquals(entries.toArray(), entryDAO.filterEntryByTitle("test", "", date2015, null).toArray());
    }

    @Test
    public void filterAfterTest() {
        List<Entry> entries = saveForFilter();
        entries.remove(2);
        Date date2025 = TestUtility.getDate(2025, 1, 1);
        assertArrayEquals(entries.toArray(), entryDAO.filterEntryByTitle(null, "", null, date2025).toArray());
    }

    @Test
    public void contentTest() {
        List<Entry> entries = saveForFilter();
        entries.remove(2);
        entries.remove(0);

        Date date2025 = TestUtility.getDate(2025, 1, 1);
        Date date2015 = TestUtility.getDate(2015, 1, 1);
        assertArrayEquals(entries.toArray(), entryDAO.filterEntryByContent("test", "", date2015, date2025).toArray());
    }
}
