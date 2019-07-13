package in.nimbo.dao;

import in.nimbo.dao.pool.ConnectionPool;
import in.nimbo.entity.Description;
import in.nimbo.exception.QueryException;
import org.apache.commons.dbutils.DbUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DescriptionDAOImpl implements DescriptionDAO {
    ConnectionPool connectionPool;

    public DescriptionDAOImpl(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    /**
     * create a list of description from a ResultSet of JDBC
     *
     * @param resultSet resultSet of database
     * @return list of description
     * @throws SQLException if unable to fetch data from ResultSet
     */
    private List<Description> createDescriptionFromResultSet(ResultSet resultSet) throws SQLException {
        List<Description> descriptions = new ArrayList<>();
        while (resultSet.next()) {
            Description description = new Description();
            description.setId(resultSet.getInt("id"));
            description.setType(resultSet.getString("type"));
            description.setMode(resultSet.getString("mode"));
            description.setValue(resultSet.getString("value"));
            description.setFeedId(resultSet.getInt("feed_id"));
            descriptions.add(description);
        }
        return descriptions;
    }

    /**
     * get list of descriptions from database which their feed_id is given
     *
     * @param feedId feed_id to search id
     * @return list of descriptions
     * @throws QueryException if unable to execute query
     */
    @Override
    public Optional<Description> getByFeedId(int feedId) {
        ResultSet resultSet = null;
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT * FROM description WHERE feed_id=?")) {
            preparedStatement.setInt(1, feedId);
            resultSet = preparedStatement.executeQuery();
            return Optional.ofNullable(createDescriptionFromResultSet(resultSet).get(0));
        } catch (IndexOutOfBoundsException e) {
            return Optional.empty();
        } catch (SQLException e) {
            throw new QueryException(e);
        } finally {
            DbUtils.closeQuietly(resultSet);
        }
    }

    /**
     * save an description in database
     *
     * @param description description which is saved
     * @return description which it's ID will be set after adding to database
     * @throws QueryException if unable to execute query
     */
    @Override
    public Description save(Description description) {
        ResultSet generatedKeys = null;
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO description(type, mode, value, feed_id) VALUES(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);) {
            preparedStatement.setString(1, description.getType());
            preparedStatement.setString(2, description.getMode());
            preparedStatement.setString(3, description.getValue());
            preparedStatement.setInt(4, description.getFeedId());
            preparedStatement.executeUpdate();
            generatedKeys = preparedStatement.getGeneratedKeys();
            generatedKeys.next();
            int newId = generatedKeys.getInt(1);
            description.setId(newId);
            return description;
        } catch (SQLException e) {
            throw new QueryException(e);
        } finally {
            DbUtils.closeQuietly(generatedKeys);
        }
    }
}
