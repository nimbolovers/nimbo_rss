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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ConnectionPool.class)
public class EntryDAOTest {
    private static ConnectionWrapper connection;
    private static EntryDAO entryDAO;

    private static String readFile(String path) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(path));
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("couldn't read file: " + path, e);
        }
    }

    @BeforeClass
    public static void init() throws Exception {
        TestUtility.disableJOOQLogo();
        connection = PowerMockito.spy(new ConnectionWrapper(DriverManager.getConnection("jdbc:h2:~/h2_rss", "user", "")));
        DescriptionDAO descriptionDAO = new DescriptionDAOImpl();
        ContentDAO contentDAO = new ContentDAOImpl();
        entryDAO = new EntryDAOImpl(descriptionDAO, contentDAO);

        PowerMockito.mockStatic(ConnectionPool.class);
        PowerMockito.when(ConnectionPool.getConnection()).thenReturn(connection);
        PowerMockito.doNothing().when(connection).close();
    }

    private void freshTables() throws SQLException {
        String queries = readFile("db/db_tables_sql.sql");
        PreparedStatement statement = connection.prepareStatement(queries);
        statement.execute();
    }

    @Before
    public void initTables() throws SQLException {
        freshTables();
        System.out.println(entryDAO.getEntries());
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
                "SELECT feed.*," +
                        " content.value as cnt" +
                        " FROM feed " +
                        "INNER JOIN content ON feed.id=content.feed_id");
        ResultSet resultSet = statement.executeQuery();
        List<Entry> list = new ArrayList<>();
        while (resultSet.next()) {
            Entry e = TestUtility.createEntry(resultSet.getString("channel"), resultSet.getString("title"),
                    resultSet.getString("link"), resultSet.getDate("pub_date"),
                    resultSet.getString("cnt"), ""
            );
            e.setId(resultSet.getInt("id"));
            list.add(e);
        }
        assertArrayEquals(entryList.toArray(), list.toArray());
    }

    private List<Entry> saveForFilter() {
        Entry entry2010 = TestUtility.createEntry("test", "title 1", "2010", getDate(2010, 1, 1), "test", "desc");
        Entry entry2020 = TestUtility.createEntry("test", "title 2", "2020", getDate(2020, 1, 1), "test", "desc");
        Entry entry2030 = TestUtility.createEntry("test", "title 3", "2030", getDate(2030, 1, 1), "test", "desc");
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
    public void getAllTest() {
        List<Entry> entries = saveForFilter();
        assertArrayEquals(entries.toArray(), entryDAO.getEntries().toArray());
    }

//    @Test
//    public void filterBeforeTest() {
//        List<Entry> entries = saveForFilter();
////        entries.remove(0);
//        Date date2015 = getDate(2015, 1, 1);
//        assertArrayEquals(entries.toArray(), entryDAO.filterEntryByTitle("test", "", null, null).toArray());
//    }
//
//    @Test
//    public void filterAfterTest() {
//        List<Entry> entries = saveForFilter();
//        entries.remove(2);
//        Date date2025 = getDate(2025, 1, 1);
//        assertArrayEquals(entries.toArray(), entryDAO.filterEntryByTitle(null, "", null, date2025).toArray());
//    }
//
//    @Test
//    public void contentTest() {
//        List<Entry> entries = saveForFilter();
//        entries.remove(2);
//        entries.remove(0);
//
//        Date date2025 = getDate(2025, 1, 1);
//        Date date2015 = getDate(2015, 1, 1);
//        assertArrayEquals(entries.toArray(), entryDAO.filterEntryByContent("test", "", date2015, date2025).toArray());
//    }

    private Date getDate(int year, int month, int day) {
        LocalDateTime dateTime = LocalDateTime.of(LocalDate.of(year, month, day), LocalTime.of(0, 0));
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

}
