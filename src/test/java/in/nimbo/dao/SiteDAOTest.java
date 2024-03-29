package in.nimbo.dao;

import in.nimbo.DAOUtility;
import in.nimbo.TestUtility;
import in.nimbo.dao.pool.ConnectionPool;
import in.nimbo.entity.Site;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SiteDAOTest {
    private static ConnectionPool connectionPool;
    private static Connection connection;
    private static Connection fakeConnection;
    private static SiteDAO siteDAO;

    @BeforeClass
    public static void init() throws SQLException, ClassNotFoundException {
        TestUtility.disableJOOQLogo();

        connection = DAOUtility.getConnection();
        connection = spy(connection);
        connectionPool = mock(ConnectionPool.class);

        siteDAO = new SiteDAOImpl(connectionPool);

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

        connection.prepareStatement("DELETE FROM site").executeUpdate();
    }

    private List<Site> createExampleSites() {
        List<Site> sites = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            Site site = new Site("site " + i, "link " + i);
            site.setLastUpdate(i % 2 == 0 ? LocalDateTime.now() : null);
            site.setAvgUpdateTime(5 * i);
            site.setNewsCount(100 * i);
            sites.add(site);
        }
        return sites;
    }

    @Test
    public void save() throws SQLException {
        List<Site> savedSites = createExampleSites();
        for (Site site : savedSites) {
            siteDAO.save(site);
        }

        PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM site");
        ResultSet resultSet = statement.executeQuery();
        List<Site> fetchedSites = new ArrayList<>();
        while (resultSet.next()) {
            Site site = new Site(resultSet.getString("name"), resultSet.getString("link"));
            site.setLastUpdate(resultSet.getObject("last_update", LocalDateTime.class));
            site.setNewsCount(resultSet.getLong("news_count"));
            site.setAvgUpdateTime(resultSet.getLong("avg_update_time"));
            site.setId(resultSet.getInt("id"));
            fetchedSites.add(site);
        }

        assertEquals(savedSites, fetchedSites);

        when(connectionPool.getConnection()).thenReturn(fakeConnection);
        try {
            siteDAO.save(savedSites.get(0));
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof QueryException);
        }
    }

    @Test
    public void getSites() {
        List<Site> savedSites = createExampleSites();
        for (Site site : savedSites) {
            siteDAO.save(site);
        }

        assertEquals(savedSites, siteDAO.getSites());

        when(connectionPool.getConnection()).thenReturn(fakeConnection);
        try {
            siteDAO.getSites();
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof QueryException);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateWithoutId() {
        Site site = new Site("name", "link");
        siteDAO.update(site);
    }

    @Test
    public void updateExists() throws SQLException {
        Site site = new Site("name", "link");
        site.setLastUpdate(LocalDateTime.now());
        site.setAvgUpdateTime(5);
        site.setNewsCount(100);
        siteDAO.save(site);

        site.setName("updated-name");
        site.setLink("updated-link");
        site.setLastUpdate(LocalDateTime.of(2000, 1, 1, 0, 0));
        site.setAvgUpdateTime(10);
        site.setNewsCount(200);
        siteDAO.update(site);

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM site");
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        assertEquals("updated-name", resultSet.getString("name"));
        assertEquals("updated-link", resultSet.getString("link"));
        assertEquals(LocalDateTime.of(2000, 1, 1, 0, 0), resultSet.getObject("last_update", LocalDateTime.class));
        assertEquals(10L, resultSet.getLong("avg_update_time"));
        assertEquals(200L, resultSet.getLong("news_count"));

        assertFalse(resultSet.next());

        when(connectionPool.getConnection()).thenReturn(fakeConnection);
        try {
            siteDAO.update(site);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof QueryException);
        }
    }

    @Test
    public void getCountSitesTest() throws SQLException {
        int count = 5;
        for (int i = 0; i < count; i++) {
            PreparedStatement statement = connection.prepareStatement("insert into site (link, news_count, avg_update_time) values (?, ?, ?)");
            statement.setString(1, "link" + i);
            statement.setInt(2, i);
            statement.setInt(3, i);
            statement.executeUpdate();
        }

        assertEquals(count, siteDAO.getCount());

        when(connectionPool.getConnection()).thenReturn(fakeConnection);
        try {
            siteDAO.getCount();
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof QueryException);
        }
    }

    @Test
    public void contain() {
        List<Site> sites = createExampleSites();
        for (Site site : sites) {
            siteDAO.save(site);
        }

        assertTrue(siteDAO.containLink("link 1"));
        assertTrue(siteDAO.containLink("link 2"));
        assertFalse(siteDAO.containLink("link 3"));

        when(connectionPool.getConnection()).thenReturn(fakeConnection);
        try {
            siteDAO.containLink("link 1");
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof QueryException);
        }
    }
}
