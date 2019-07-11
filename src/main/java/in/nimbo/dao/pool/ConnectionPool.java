package in.nimbo.dao.pool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import in.nimbo.exception.ConnectionException;
import in.nimbo.exception.PropertyNotFoundException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionPool {
    private static HikariDataSource dataSource;
    private static Properties databaseProp;
    private static Properties hikariProperties;

    private ConnectionPool() {}

    public static Properties getProperties() {
        if (hikariProperties == null || databaseProp == null){
            try {
                hikariProperties = new Properties();
                databaseProp = new Properties();
                ClassLoader loader = ConnectionPool.class.getClassLoader();
                databaseProp.load(loader.getResourceAsStream("database.properties"));
                hikariProperties.load(loader.getResourceAsStream("hikari.properties"));
            } catch (IOException e) {
                throw new PropertyNotFoundException("HikariCP properties not found", e);
            }
        }
        return hikariProperties;
    }

    /**
     * initialize and configure datasource
     */
    private static void init() {
        HikariConfig cfg = new HikariConfig();
        getProperties();
        cfg.setJdbcUrl(databaseProp.getProperty("database.url"));
        cfg.setUsername(databaseProp.getProperty("database.username"));
        cfg.setPassword(databaseProp.getProperty("database.password"));
        cfg.setDriverClassName(databaseProp.getProperty("database.driver"));
        for (String key:hikariProperties.stringPropertyNames()) {
            cfg.addDataSourceProperty(key, hikariProperties.getProperty(key));
        }
        dataSource = new HikariDataSource(cfg);

    }

    /**
     * create a connection to database
     * @return connection which is created
     */
    public static Connection getConnection(){
        if (dataSource == null)
            init();
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new ConnectionException("Unable to get connection from datasource", e);
        }
    }
}
