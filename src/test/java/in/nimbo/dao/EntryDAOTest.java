package in.nimbo.dao;

import in.nimbo.DAOUtility;
import in.nimbo.TestUtility;
import in.nimbo.dao.pool.ConnectionPool;
import in.nimbo.entity.Entry;
import in.nimbo.entity.report.DateReport;
import in.nimbo.entity.report.HourReport;
import in.nimbo.entity.report.Report;
import in.nimbo.exception.QueryException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EntryDAOTest {
    private static ConnectionPool connectionPool;
    private static Connection connection;
    private static Connection fakeConnection;
    private static EntryDAO entryDAO;

    @BeforeClass
    public static void init() throws SQLException, ClassNotFoundException {
        TestUtility.disableJOOQLogo();

        connection = DAOUtility.getConnection();
        connection = spy(connection);
        connectionPool = mock(ConnectionPool.class);

        DescriptionDAO descriptionDAO = new DescriptionDAOImpl(connectionPool);
        ContentDAO contentDAO = new ContentDAOImpl(connectionPool);
        entryDAO = new EntryDAOImpl(connectionPool, descriptionDAO, contentDAO);

        fakeConnection = DAOUtility.getFakeConnection();
    }

    @AfterClass
    public static void finish() throws SQLException {
        if (connection != null)
            connection.close();
    }

    @Before
    public void initBeforeEachTest() throws SQLException {
        when(connectionPool.getConnection()).thenReturn(connection);
        doNothing().when(connection).close();

        connection.prepareStatement("DELETE FROM content;" +
                "DELETE FROM description;" +
                "DELETE FROM feed").executeUpdate();
    }

    private List<Entry> createExampleEntries1() {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Entry entry = TestUtility.createEntry("channel-" + i, "title-" + i, "link-" + i,
                    (i & 1) != 0 ? LocalDateTime.now() : null,
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
                    resultSet.getString("link"), resultSet.getObject("pub_date", LocalDateTime.class),
                    resultSet.getString("cnt"), "");
            e.setId(resultSet.getInt("id"));
            fetchedEntries.add(e);
        }

        assertEquals(entries, fetchedEntries);

        when(connectionPool.getConnection()).thenReturn(fakeConnection);
        try {
            entryDAO.save(entries.get(0));
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof QueryException);
        }
    }

    @Test
    public void getEntries() {
        List<Entry> entries = createExampleEntries1();
        for (Entry entry : entries) {
            entryDAO.save(entry);
        }

        assertEquals(entries, entryDAO.getEntries());

        when(connectionPool.getConnection()).thenReturn(fakeConnection);
        try {
            entryDAO.getEntries();
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof QueryException);
        }
    }

    private List<Entry> createExampleEntries2() {
        List<Entry> entries = new ArrayList<>();
        entries.add(TestUtility.createEntry("channel", "title 1", "2010", LocalDateTime.of(2010, 1, 1, 0, 0), "content 1", "desc 1"));
        entries.add(TestUtility.createEntry("channel", "title 2", "2020",  LocalDateTime.of(2020, 1, 1, 0, 0), "content 2", "desc 2"));
        return entries;
    }

    @Test
    public void filterEntry() {
        List<Entry> entries = createExampleEntries2();
        for (Entry entry : entries) {
            entryDAO.save(entry);
        }

        // test before
        LocalDateTime beforeDate = LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime afterDate = LocalDateTime.of(2030, 1, 1, 0, 0);

        assertEquals(entries.stream()
                        .filter(entry -> entry.getPublicationDate().compareTo(beforeDate) >= 0)
                        .collect(Collectors.toList()),
                entryDAO.filterEntry(null, "content", "title", beforeDate, null));
        // test after
        assertEquals(entries.stream()
                        .filter(entry -> entry.getPublicationDate().compareTo(beforeDate) >= 0)
                        .filter(entry -> entry.getPublicationDate().compareTo(afterDate) <= 0)
                        .collect(Collectors.toList()),
                entryDAO.filterEntry("", "content", "title", beforeDate, afterDate));
        // test between
        assertEquals(entries.stream()
                        .filter(entry -> entry.getPublicationDate().compareTo(afterDate) <= 0)
                        .collect(Collectors.toList()),
                entryDAO.filterEntry("channel", "content", "title", null, afterDate));

        when(connectionPool.getConnection()).thenReturn(fakeConnection);
        try {
            entryDAO.filterEntry("", "", "", LocalDateTime.now(), LocalDateTime.now());
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof QueryException);
        }
    }

    @Test
    public void contain() {
        List<Entry> entries = new ArrayList<>();
        entries.add(TestUtility.createEntry("channel 1", "title 1", "link 1", LocalDateTime.now(), "content 1", "desc 1"));
        entries.add(TestUtility.createEntry("channel 2", "title 2", "link 2", LocalDateTime.now(), "content 2", "desc 2"));
        for (Entry entry : entries) {
            entryDAO.save(entry);
        }

        assertTrue(entryDAO.contain(TestUtility.createEntry("channel-test", "title-test", "link 1", LocalDateTime.now(), "content-test", "desc-test")));
        assertTrue(entryDAO.contain(TestUtility.createEntry("channel-test", "title-test", "link 2", LocalDateTime.now(), "content-test", "desc-test")));
        assertFalse(entryDAO.contain(TestUtility.createEntry("channel-test", "title-test", "link 3", LocalDateTime.now(), "content-test", "desc-test")));

        when(connectionPool.getConnection()).thenReturn(fakeConnection);
        try {
            entryDAO.contain(entries.get(0));
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof QueryException);
        }
    }

    @Test
    public void getDateReportsTest() throws SQLException {
        String sql = "insert into feed (channel, title, pub_date) values (?, ?, ?)";
        List<DateReport> reports = new ArrayList<>();
        String channel = "test";
        String title = "test";
        int limit = 10;
        for (int i = 0; i < limit; i++) {
            int count = ThreadLocalRandom.current().nextInt(10) + 1;
            for (int j = 0; j < count; j++) {
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, channel);
                statement.setString(2, title);
                statement.setObject(3, LocalDateTime.of(2010, 6, i + 1, 0, 0));
                statement.executeUpdate();
            }
            int year = 2010;
            int month = 6;
            int day = i + 1;
            DateReport report = new DateReport(channel, count,  LocalDateTime.of(year, month, day, 0, 0));
            reports.add(report);
        }

        Collections.reverse(reports);

        assertEquals(reports, entryDAO.getDateReports("", limit));
        assertEquals(reports, entryDAO.getDateReports(null, limit));

        when(connectionPool.getConnection()).thenReturn(fakeConnection);
        try {
            entryDAO.getDateReports("", 0);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof QueryException);
        }
    }

    @Test
    public void getAllReportsTest() throws SQLException {
        String sql = "insert into feed (channel, title, pub_date) values (?, ?, ?)";
        List<Report> reports = new ArrayList<>();
        List<Report> allReports = new ArrayList<>();
        String channel = "test";
        String title = "test";
        int limit = 10;
        int cnt = 0;
        for (int i = 0; i < limit; i++) {
            int count = ThreadLocalRandom.current().nextInt(limit) + 1;
            for (int j = 0; j < count; j++) {
                LocalDateTime date = LocalDateTime.of(2015, 6, i + 1, i, 0);
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, channel);
                statement.setString(2, title);
                statement.setObject(3, date);
                statement.executeUpdate();
            }
            cnt += count;
            if (i + 1 == 8) {
                Report report = new Report(channel, count);
                reports.add(report);
            }
        }

        allReports.add(new Report(channel, cnt));

        List<Report> real = entryDAO.getAllReports(null, LocalDateTime.of(2015, 6, 8, 0, 0));
        assertEquals(reports, real);

        real = entryDAO.getAllReports("", null);
        assertEquals(allReports, real);

        when(connectionPool.getConnection()).thenReturn(fakeConnection);
        try {
            entryDAO.getAllReports(null, LocalDateTime.of(2015, 6, 8, 0, 0));
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof QueryException);
        }
    }

    @Test
    public void getHourReportTest() throws SQLException {
        String sql = "insert into feed (channel, title, pub_date) values (?, ?, ?)";
        Set<HourReport> reports = new HashSet<>();
        String channel = "test";
        String title = "test";
        int limit = 10;
        for (int i = 0; i < limit; i++) {
            int count = ThreadLocalRandom.current().nextInt(limit) + 1;
            for (int j = 0; j < count; j++) {
                LocalDateTime date = LocalDateTime.of(2010, 6, i + 1, i, 0);
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, channel);
                statement.setString(2, title);
                statement.setObject(3, date);
                statement.executeUpdate();
            }
            HourReport report = new HourReport(channel, count, i);
            reports.add(report);
        }

        List<HourReport> realAnswer = entryDAO.getHourReports(title, "");
        Set<HourReport> hourReports = new HashSet<>(realAnswer);
        assertEquals(hourReports, reports);

        realAnswer = entryDAO.getHourReports(null, null);
        hourReports = new HashSet<>(realAnswer);
        assertEquals(hourReports, reports);

        when(connectionPool.getConnection()).thenReturn(fakeConnection);
        try {
            entryDAO.getHourReports("", "");
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof QueryException);
        }
    }
}
