package in.nimbo.dao;

import in.nimbo.dao.pool.ConnectionPool;
import in.nimbo.dao.pool.ConnectionWrapper;
import in.nimbo.entity.Content;
import in.nimbo.exception.RecordNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContentDAOImpl implements ContentDAO {
    private Logger logger = LoggerFactory.getLogger(ContentDAOImpl.class);

    /**
     * create a list of contents from a ResultSet of JDBC
     *
     * @param resultSet resultSet of database
     * @return list of contents
     * @throws RuntimeException if unable to fetch data from ResultSet
     */
    private List<Content> createContentFromResultSet(ResultSet resultSet) {
        List<Content> contents = new ArrayList<>();
        try {
            while (resultSet.next()) {
                Content content = new Content();

                // fetch id
                content.setId(resultSet.getInt(1));

                // fetch value
                content.setValue(resultSet.getString(2));

                // fetch feed_id
                content.setFeed_id(resultSet.getInt(3));

                contents.add(content);
            }
        } catch (SQLException e) {
            logger.error("Unable to fetch data from ResultSet: " + e.getMessage());
            throw new RuntimeException("Unable to fetch data from ResultSet", e);
        }
        return contents;
    }

    /**
     * get list of contents from database which their feed_id is given
     *
     * @param feedId feed_id to search id
     * @return list of contents
     * @throws RecordNotFoundException if unable to find a record with given feedId
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
            logger.error("Unable to execute query: " + e.getMessage());
            throw new RuntimeException("Unable to execute query", e);
        }
    }

    /**
     * save an content in database
     *
     * @param content content which is saved
     * @return content which it's ID will be set after adding to database
     */
    @Override
    public Content save(Content content) {
        try (ConnectionWrapper connection = ConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO content(value, feed_id) VALUES(?, ?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, content.getValue());
            preparedStatement.setInt(2, content.getFeed_id());
            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            generatedKeys.next();
            int newId = generatedKeys.getInt(1);
            content.setId(newId);
        } catch (SQLException e) {
            logger.error("Unable to execute query: " + e.getMessage());
            throw new RuntimeException("Unable to execute query", e);
        }
        return content;
    }
}
