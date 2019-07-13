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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ContentDAOTest {
    private static ConnectionPool connectionPool;
    private static Connection connection;
    private static Connection fakeConnection;
    private static ContentDAO contentDAO;

    @BeforeClass
    public static void init() throws SQLException, ClassNotFoundException {
        TestUtility.disableJOOQLogo();

        connection = DAOUtility.getConnection();
        connection = spy(connection);
        connectionPool = mock(ConnectionPool.class);

        contentDAO = new ContentDAOImpl(connectionPool);

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

        connection.prepareStatement("DELETE FROM content").executeUpdate();
    }

    @Test
    public void getByFeedIdNotExists() {
        Optional<Content> byFeedId = contentDAO.getByFeedId(1);
        assertFalse(byFeedId.isPresent());

        when(connectionPool.getConnection()).thenReturn(fakeConnection);
        try {
            contentDAO.getByFeedId(1);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof QueryException);
        }
    }

    @Test(expected = QueryException.class)
    public void saveWithException() {
        when(connectionPool.getConnection()).thenReturn(fakeConnection);
        contentDAO.save(new Content());
    }
}