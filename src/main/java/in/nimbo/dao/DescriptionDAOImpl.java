package in.nimbo.dao;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import in.nimbo.dao.pool.ConnectionPool;
import in.nimbo.dao.pool.ConnectionWrapper;
import in.nimbo.entity.Description;
import in.nimbo.exception.RecordNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DescriptionDAOImpl implements DescriptionDAO {
    private Logger logger = LoggerFactory.getLogger(DescriptionDAOImpl.class);

    /**
     * create a list of description from a ResultSet of JDBC
     *
     * @param resultSet resultSet of database
     * @return list of description
     */
    private List<Description> createDescriptionFromResultSet(ResultSet resultSet) {
        List<Description> descriptions = new ArrayList<>();
        try {
            while (resultSet.next()) {
                Description description = new Description();
                SyndContent syndContent = new SyndContentImpl();
                description.setSyndContent(syndContent);

                // fetch id
                description.setId(resultSet.getInt(1));

                // fetch type
                syndContent.setType(resultSet.getString(2));

                // fetch mode
                syndContent.setMode(resultSet.getString(3));

                // fetch value
                syndContent.setValue(resultSet.getString(4));

                // fetch feed_id
                description.setFeed_id(resultSet.getInt(5));

                descriptions.add(description);
            }
        } catch (SQLException e) {
            logger.error("Unable to fetch data from ResultSet: " + e.getMessage());
            throw new RuntimeException("Unable to fetch data from ResultSet", e);
        }
        return descriptions;
    }

    /**
     * get list of descriptions from database which their feed_id is given
     *
     * @param feedId feed_id to search id
     * @return list of descriptions
     * @throws RecordNotFoundException if unable to find a record with given feedId
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
            logger.error("Unable to execute query: " + e.getMessage());
            throw new RuntimeException("Unable to execute query", e);
        }
    }

    /**
     * save an description in database
     *
     * @param description description which is saved
     * @return description which it's ID will be set after adding to database
     */
    @Override
    public Description save(Description description) {
        try (ConnectionWrapper connection = ConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO description(type, mode, value, feed_id) VALUES(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, description.getSyndContent().getType());
            preparedStatement.setString(2, description.getSyndContent().getMode());
            preparedStatement.setString(3, description.getSyndContent().getValue());
            preparedStatement.setInt(4, description.getFeed_id());
            int newId = preparedStatement.executeUpdate();
            description.setId(newId);
        } catch (SQLException e) {
            logger.error("Unable to execute query: " + e.getMessage());
            throw new RuntimeException("Unable to execute query", e);
        }
        return description;
    }
}
