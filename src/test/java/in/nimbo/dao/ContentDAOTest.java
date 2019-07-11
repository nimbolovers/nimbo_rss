package in.nimbo.dao;

import in.nimbo.DAOUtility;
import in.nimbo.TestUtility;
import in.nimbo.dao.pool.ConnectionPool;
import in.nimbo.entity.Content;
import in.nimbo.exception.QueryException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ConnectionPool.class)
public class ContentDAOTest {
    private static Connection connection;
    private static Connection fakeConnection;
    private static ContentDAO contentDAO;

    @BeforeClass
    public static void init() throws SQLException, ClassNotFoundException {
        TestUtility.disableJOOQLogo();

        contentDAO = new ContentDAOImpl();

        connection = DAOUtility.getConnection();
        connection = PowerMockito.spy(connection);

        fakeConnection = DAOUtility.getFakeConnection();
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

        connection.prepareStatement("DELETE FROM content").executeUpdate();
    }

    @Test
    public void getByFeedIdNotExists() {
        Optional<Content> byFeedId = contentDAO.getByFeedId(1);
        assertFalse(byFeedId.isPresent());

        PowerMockito.when(ConnectionPool.getConnection()).thenReturn(fakeConnection);
        try {
            contentDAO.getByFeedId(1);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof QueryException);
        }
    }

    @Test(expected = QueryException.class)
    public void saveWithException() {
        PowerMockito.when(ConnectionPool.getConnection()).thenReturn(fakeConnection);
        contentDAO.save(new Content());
    }
}