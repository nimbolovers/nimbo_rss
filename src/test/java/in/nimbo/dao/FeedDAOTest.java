package in.nimbo.dao;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.Scanner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class FeedDAOTest
{
    private ContentDAO contentDAO;
    private static Connection connection;
    @BeforeClass
    public static void init() throws SQLException, FileNotFoundException {
        connection = DriverManager.getConnection("jdbc:h2:~/h2_rss", "user", "");
        Scanner scanner = new Scanner(new FileInputStream("db/db_sql.sql"));
        String queries = "";
        while (scanner.hasNextLine()){
            queries += scanner.nextLine();
        }
        String[] split = queries.split(";");
        for (String query:split) {
            PreparedStatement preparedStatement = connection.prepareStatement(query + ";");
            preparedStatement.execute();
        }
        PreparedStatement statement = connection.prepareStatement("SHOW TABLES");
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()){
            System.out.println(resultSet);
        }

    }
    @Test
    public void testConnection()
    {
        contentDAO = mock(ContentDAO.class);
        FeedDAO feedDAO = new FeedDAOImpl(contentDAO);
    }
}
