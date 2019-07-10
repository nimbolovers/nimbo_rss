package in.nimbo.dao;

import in.nimbo.dao.pool.ConnectionPool;
import in.nimbo.dao.pool.ConnectionWrapper;
import in.nimbo.entity.Content;
import in.nimbo.exception.QueryException;
import in.nimbo.exception.RecordNotFoundException;
import in.nimbo.exception.ResultSetFetchException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ContentDAOImpl implements ContentDAO {
    /**
     * create a list of contents from a ResultSet of JDBC
     *
     * @param resultSet resultSet of database
     * @return list of contents
     * @throws ResultSetFetchException if unable to fetch data from ResultSet
     */
    private List<Content> createContentFromResultSet(ResultSet resultSet) {
        List<Content> contents = new ArrayList<>();
        try {
            while (resultSet.next()) {
                Content content = new Content();
                content.setId(resultSet.getInt("id"));
                content.setValue(resultSet.getString("value"));
                content.setFeedId(resultSet.getInt("feed_id"));
                contents.add(content);
            }
        } catch (SQLException e) {
            throw new ResultSetFetchException("Unable to fetch data from ResultSet", e);
        }
        return contents;
    }

    /**
     * get list of contents from database which their feed_id is given
     *
     * @param feedId feed_id to search id
     * @return list of contents
     * @throws RecordNotFoundException if unable to find a record with given feedId
     * @throws QueryException if unable to execute query
     */
    @Override
    public Content getByFeedId(int feedId) {
        try (ConnectionWrapper connection = ConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT * FROM content WHERE feed_id=?");
            preparedStatement.setInt(1, feedId);
            ResultSet resultSet = preparedStatement.executeQuery();
            return createContentFromResultSet(resultSet).get(0);
        } catch (IndexOutOfBoundsException e) {
            throw new RecordNotFoundException("content which has feed_id=" + feedId + " not found", e);
        } catch (SQLException e) {
            throw new QueryException("Unable to execute query", e);
        }
    }

    /**
     * save an content in database
     *
     * @param content content which is saved
     * @return content which it's ID will be set after adding to database
     * @throws QueryException if unable to execute query
     */
    @Override
    public Content save(Content content) {
        try (ConnectionWrapper connection = ConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO content(value, feed_id) VALUES(?, ?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, content.getValue());
            preparedStatement.setInt(2, content.getFeedId());
            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            generatedKeys.next();
            int newId = generatedKeys.getInt(1);
            content.setId(newId);
        } catch (SQLException e) {
            throw new QueryException("Unable to execute query", e);
        }
        return content;
    }
}
