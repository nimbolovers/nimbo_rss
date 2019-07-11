package in.nimbo.dao;

import in.nimbo.dao.pool.ConnectionPool;
import in.nimbo.entity.Site;
import in.nimbo.exception.QueryException;
import org.apache.commons.dbutils.DbUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SiteDAOImpl implements SiteDAO {
    /**
     * create a list of sites from a ResultSet of JDBC
     *
     * @param resultSet resultSet of database
     * @return list of sites
     * @throws SQLException if unable to fetch data from ResultSet
     */
    private List<Site> createSiteFromResultSet(ResultSet resultSet) throws SQLException {
        List<Site> sites = new ArrayList<>();
        while (resultSet.next()) {
            Site site = new Site();
            site.setId(resultSet.getInt("id"));
            site.setName(resultSet.getString("name"));
            site.setLink(resultSet.getString("link"));
            site.setNewsCount(resultSet.getLong("news_count"));
            site.setAvgUpdateTime(resultSet.getLong("avg_update_time"));
            site.setLastUpdate(resultSet.getObject("last_update", LocalDateTime.class));
            sites.add(site);
        }
        return sites;
    }

    /**
     * fetch all of sites in database
     *
     * @return a list of sites
     * @throws QueryException if unable to execute query
     */
    @Override
    public List<Site> getSites() {
        try (Connection connection = ConnectionPool.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM site");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            return createSiteFromResultSet(resultSet);
        } catch (SQLException e) {
            throw new QueryException(e);
        }
    }

    /**
     * check whether exists a site with given link
     *
     * @param link link of site
     * @return true if exists a site with given link
     */
    @Override
    public boolean containLink(String link) {
        ResultSet resultSet = null;
        try (Connection connection = ConnectionPool.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM site where link = ?")) {
            preparedStatement.setString(1, link);
            resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new QueryException(e);
        } finally {
            DbUtils.closeQuietly(resultSet);
        }
    }

    /**
     * save an site in database
     *
     * @param site site which is saved
     * @return site which it's ID will be set after adding to database
     * @throws QueryException if unable to execute query
     */
    @Override
    public Site save(Site site) {
        ResultSet generatedKeys = null;
        try (Connection connection = ConnectionPool.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO site(name, link, news_count, avg_update_time, last_update) VALUES(?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, site.getName());
            preparedStatement.setString(2, site.getLink());
            preparedStatement.setLong(3, site.getNewsCount());
            preparedStatement.setLong(4, site.getAvgUpdateTime());
            preparedStatement.setObject(5, site.getLastUpdate());
            preparedStatement.executeUpdate();

            generatedKeys = preparedStatement.getGeneratedKeys();
            generatedKeys.next();
            int newId = generatedKeys.getInt(1);
            site.setId(newId);
            return site;
        } catch (SQLException e) {
            throw new QueryException(e);
        } finally {
            DbUtils.closeQuietly(generatedKeys);
        }
    }

    /**
     * update an site in database
     *
     * @param site site which is update
     * @return given site
     * @throws IllegalAccessError if site id is not set
     * @throws QueryException     if unable to execute query
     */
    @Override
    public Site update(Site site) {
        if (site.getId() == 0)
            throw new IllegalArgumentException("Site id must be set for update operation");
        try (Connection connection = ConnectionPool.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "UPDATE site SET link = ?, name = ?, news_count = ?, avg_update_time = ?, last_update = ? WHERE id = ?")) {
            preparedStatement.setString(1, site.getLink());
            preparedStatement.setString(2, site.getName());
            preparedStatement.setLong(3, site.getNewsCount());
            preparedStatement.setLong(4, site.getAvgUpdateTime());
            preparedStatement.setObject(5, site.getLastUpdate());
            preparedStatement.setInt(6, site.getId());
            preparedStatement.executeUpdate();
            return site;
        } catch (SQLException e) {
            throw new QueryException(e);
        }
    }

    /**
     * @return the sites' count
     */
    @Override
    public int getCount() {
        try (Connection connection = ConnectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement("select count(*) as cnt from site");
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt("cnt");
        } catch (SQLException e) {
            throw new QueryException(e);
        }
    }
}
