package in.nimbo.dao;

import in.nimbo.TestUtility;
import in.nimbo.dao.pool.ConnectionPool;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.*;

public class ConnectionPoolTest {
    @BeforeClass
    public static void init() {
        TestUtility.disableJOOQLogo();
    }

    @Test
    public void testHikari() {
        ConnectionPool connectionPool = new ConnectionPool();
        try (Connection connection = connectionPool.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SHOW TABLES")) {
            assertNotNull(resultSet);
        } catch (SQLException e) {
            fail();
        }

        try (Connection connection = connectionPool.getConnection()){
            assertNotNull(connection);
        } catch (SQLException e) {
            fail();
        }
    }
}
