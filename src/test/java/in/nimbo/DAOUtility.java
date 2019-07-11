package in.nimbo;

import in.nimbo.dao.FakeConnection;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DAOUtility {
    private static Connection connection;
    private static Connection fakeConnection;

    private DAOUtility() {}

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        if (connection != null)
            return connection;
        String initialH2Query = TestUtility.getFileContent(Paths.get("db/db_tables_sql.sql"));
        Class.forName(TestUtility.getDatabaseProperties().getProperty("database.driver"));
        connection = DriverManager.getConnection(
                TestUtility.getDatabaseProperties().getProperty("database.url"),
                TestUtility.getDatabaseProperties().getProperty("database.username"),
                TestUtility.getDatabaseProperties().getProperty("database.password"));

        connection.prepareStatement(initialH2Query).executeUpdate();
        return connection;
    }

    public static Connection getFakeConnection() {
        if (fakeConnection != null)
            return fakeConnection;

        return fakeConnection = new FakeConnection();
    }
}
