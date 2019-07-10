package in.nimbo.dao;

import in.nimbo.dao.pool.ConnectionPool;
import in.nimbo.dao.pool.ConnectionWrapper;
import in.nimbo.entity.Description;
import in.nimbo.exception.QueryException;
import in.nimbo.exception.RecordNotFoundException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DescriptionDAOImpl implements DescriptionDAO {
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
     * @throws RecordNotFoundException if unable to find a record with given feedId
     * @throws QueryException          if unable to execute query
     */
    @Override
    public Description getByFeedId(int feedId) {
        try (ConnectionWrapper connection = ConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT * FROM description WHERE feed_id=?");
            preparedStatement.setInt(1, feedId);
            ResultSet resultSet = preparedStatement.executeQuery();
            return createDescriptionFromResultSet(resultSet).get(0);
        } catch (IndexOutOfBoundsException e) {
            throw new RecordNotFoundException("content which has feed_id=" + feedId + " not found", e);
        } catch (SQLException e) {
            throw new QueryException("Unable to execute query", e);
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
        try (ConnectionWrapper connection = ConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO description(type, mode, value, feed_id) VALUES(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, description.getType());
            preparedStatement.setString(2, description.getMode());
            preparedStatement.setString(3, description.getValue());
            preparedStatement.setInt(4, description.getFeedId());
            int newId = preparedStatement.executeUpdate();
            description.setId(newId);
        } catch (SQLException e) {
            throw new QueryException("Unable to execute query", e);
        }
        return description;
    }
}
