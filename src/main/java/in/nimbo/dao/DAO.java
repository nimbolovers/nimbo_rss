package in.nimbo.dao;

import java.sql.Connection;
import java.sql.SQLException;

public class DAO {
    private static Connection connection;
    /**
     * create a connection to database
     * @return connection which is created
     */
    public static Connection getConnection() {
        if (connection != null){
            return connection;
        }
        try {
            return connection = ConnectionPool.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Error establishing database connection", e);
        }
    }

}
