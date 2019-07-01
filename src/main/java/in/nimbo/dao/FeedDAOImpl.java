package in.nimbo.dao;

import in.nimbo.entity.Entry;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Properties;

public class FeedDAOImpl implements FeedDAO {
    private Connection connection;
    private Properties databaseProp;

    private void loadProperties() {
        try {
            databaseProp = new Properties();
            databaseProp.load(new FileInputStream("resource/database.properties"));
        } catch (IOException e) {
            throw new RuntimeException("database properties not found");
        }
    }

    public FeedDAOImpl() {
        loadProperties();
        String url = databaseProp.getProperty("database.url");
        String username = databaseProp.getProperty("database.username");
        String password = databaseProp.getProperty("database.password");
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
    public List<Entry> filterFeeds(String title) {
        return null;
    }

    @Override
    public List<Entry> getFeeds() {
        
        return null;
    }

    @Override
    public Entry save(Entry entry) {
        return null;
    }
}
