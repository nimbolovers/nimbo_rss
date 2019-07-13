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

    public ConnectionPool() {
        loadProperties();
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(databaseProp.getProperty("database.url"));
        hikariConfig.setUsername(databaseProp.getProperty("database.username"));
        hikariConfig.setPassword(databaseProp.getProperty("database.password"));
        hikariConfig.setDriverClassName(databaseProp.getProperty("database.driver"));
        for (String key : hikariProperties.stringPropertyNames()) {
            hikariConfig.addDataSourceProperty(key, hikariProperties.getProperty(key));
        }
        dataSource = new HikariDataSource(hikariConfig);
    }

    private void loadProperties() {
        try {
            if (hikariProperties == null || databaseProp == null) {
                hikariProperties = new Properties();
                databaseProp = new Properties();
                ClassLoader loader = ConnectionPool.class.getClassLoader();
                databaseProp.load(loader.getResourceAsStream("database.properties"));
                hikariProperties.load(loader.getResourceAsStream("hikari.properties"));
            }
        } catch (IOException e) {
            throw new ConnectionException("Unable to connect to database", e);
        }
    }

    /**
     * create a connection to database
     *
     * @return connection which is created
     */
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new ConnectionException("Unable to get connection from datasource", e);
        }
    }
}
