package in.nimbo.dao;

import com.rometools.rome.feed.synd.SyndEntry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class FeedDAOImpl implements FeedDAO {
    private static final String url = "jdbc:mysql://localhost:3306/nimbo_rss?useSSL=false&useUnicode=yes&characterEncoding=UTF-8";
    private static final String username = "root";
    private static final String password = "";

    private Connection connection;

    public FeedDAOImpl() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to connect to MySQL: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL driver not found");
        }
    }

    @Override
    public List<SyndEntry> getFeeds(String title) {

        return null;
    }

    @Override
    public List<SyndEntry> getFeeds() {
        return null;
    }

    @Override
    public SyndEntry save(SyndEntry entry) {
        return null;
    }
}
