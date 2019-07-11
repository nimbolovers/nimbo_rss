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
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.Connection;
import java.sql.SQLException;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ConnectionPool.class)
public class DescriptionDAOTest {
    private static Connection connection;
    private static Connection fakeConnection;
    private static DescriptionDAO descriptionDAO;

    @BeforeClass
    public static void init() throws SQLException, ClassNotFoundException {
        TestUtility.disableJOOQLogo();

        descriptionDAO = new DescriptionDAOImpl();

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

        connection.prepareStatement("DELETE FROM description").executeUpdate();
    }

    @Test(expected = QueryException.class)
    public void getByFeedIdWithException() {
        PowerMockito.when(ConnectionPool.getConnection()).thenReturn(fakeConnection);
        descriptionDAO.getByFeedId(1);
    }

    @Test(expected = QueryException.class)
    public void saveWithException() {
        PowerMockito.when(ConnectionPool.getConnection()).thenReturn(fakeConnection);
        descriptionDAO.save(new Description());
    }
}