package in.nimbo.dao;

import in.nimbo.DAOUtility;
import in.nimbo.TestUtility;
import in.nimbo.dao.pool.ConnectionPool;
import in.nimbo.entity.Description;
import in.nimbo.exception.QueryException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Mockito.*;

public class DescriptionDAOTest {
    private static ConnectionPool connectionPool;
    private static Connection connection;
    private static Connection fakeConnection;
    private static DescriptionDAO descriptionDAO;

    @BeforeClass
    public static void init() throws SQLException, ClassNotFoundException {
        TestUtility.disableJOOQLogo();

        connection = DAOUtility.getConnection();
        connection = spy(connection);
        connectionPool = mock(ConnectionPool.class);

        descriptionDAO = new DescriptionDAOImpl(connectionPool);

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

        connection.prepareStatement("DELETE FROM description").executeUpdate();
    }

    @Test(expected = QueryException.class)
    public void getByFeedIdWithException() {
        when(connectionPool.getConnection()).thenReturn(fakeConnection);
        descriptionDAO.getByFeedId(1);
    }

    @Test(expected = QueryException.class)
    public void saveWithException() {
        when(connectionPool.getConnection()).thenReturn(fakeConnection);
        descriptionDAO.save(new Description());
    }
}