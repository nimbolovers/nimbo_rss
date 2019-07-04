package in.nimbo.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionPool {
    private static HikariDataSource dataSource;
    private static Properties databaseProp;
    private static Properties hikariProperties;

    public static Properties getHikariProperties() {
        if (hikariProperties == null){
            try {
                hikariProperties = new Properties();
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                InputStream is = loader.getResourceAsStream("hikari.properties");
                hikariProperties.load(is);
            } catch (IOException e) {
                throw new RuntimeException("HikariCP properties not found", e);
            }
        }
        return hikariProperties;
    }

    static {
        HikariConfig cfg = new HikariConfig();
        getProperties();
        cfg.setJdbcUrl(databaseProp.getProperty("database.url"));
        cfg.setUsername(databaseProp.getProperty("database.username"));
        cfg.setPassword(databaseProp.getProperty("database.password"));
        cfg.setDriverClassName(databaseProp.getProperty("database.driver"));
        getHikariProperties();
        for (String key:hikariProperties.stringPropertyNames()) {
            cfg.addDataSourceProperty(key, hikariProperties.getProperty(key));
        }
        dataSource = new HikariDataSource(cfg);

    }

    /**
     * get properties of class from resource
     * @return properties which is loaded
     */
    private static Properties getProperties() {
        if (databaseProp == null) {
            try {
                databaseProp = new Properties();
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                InputStream is = loader.getResourceAsStream("database.properties");
                databaseProp.load(is);
            } catch (IOException e) {
                throw new RuntimeException("Database properties not found", e);
            }
        }
        return databaseProp;
    }

    /**
     * create a connection to database
     * @return connection which is created
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
