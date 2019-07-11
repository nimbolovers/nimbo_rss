package in.nimbo.dao.pool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import in.nimbo.exception.ConnectionException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionPool {
    private static HikariDataSource dataSource;
    private static Properties databaseProp;
    private static Properties hikariProperties;

    private ConnectionPool() {
    }

    private static void loadProperties() throws IOException {
        if (hikariProperties == null || databaseProp == null) {
            hikariProperties = new Properties();
            databaseProp = new Properties();
            ClassLoader loader = ConnectionPool.class.getClassLoader();
            databaseProp.load(loader.getResourceAsStream("database.properties"));
            hikariProperties.load(loader.getResourceAsStream("hikari.properties"));
        }
    }

    /**
     * initialize and configure datasource
     */
    private static void init() throws IOException {
        HikariConfig cfg = new HikariConfig();
        loadProperties();
        cfg.setJdbcUrl(databaseProp.getProperty("database.url"));
        cfg.setUsername(databaseProp.getProperty("database.username"));
        cfg.setPassword(databaseProp.getProperty("database.password"));
        cfg.setDriverClassName(databaseProp.getProperty("database.driver"));
        for (String key : hikariProperties.stringPropertyNames()) {
            cfg.addDataSourceProperty(key, hikariProperties.getProperty(key));
        }
        dataSource = new HikariDataSource(cfg);

    }

    /**
     * create a connection to database
     *
     * @return connection which is created
     */
    public static Connection getConnection() {
        try {
            if (dataSource == null)
                init();
            return dataSource.getConnection();
        } catch (SQLException | IOException e) {
            throw new ConnectionException("Unable to get connection from datasource", e);
        }
    }
}
