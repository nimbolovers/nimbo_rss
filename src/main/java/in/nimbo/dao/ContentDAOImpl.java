package in.nimbo.dao;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import in.nimbo.entity.Content;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ContentDAOImpl extends DAO implements ContentDAO {
    private List<Content> createContentFromResultSet(ResultSet resultSet) {
        List<Content> contents = new ArrayList<>();
        try {
            while (resultSet.next()) {
                Content content = new Content();
                SyndContent syndContent = new SyndContentImpl();
                content.setSyndContent(syndContent);

                // fetch id
                content.setId(resultSet.getInt(1));

                // fetch type
                syndContent.setType(resultSet.getString(2));

                // fetch relation
                content.setRelation(resultSet.getString(3));

                // fetch mode
                syndContent.setMode(resultSet.getString(4));

                // fetch value
                syndContent.setValue(resultSet.getString(5));

                // fetch feed_id
                content.setFeed_id(resultSet.getInt(6));

                contents.add(content);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contents;
    }

    @Override
    public Content get(int id) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(
                    "SELECT * FROM content WHERE id=?");
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            return createContentFromResultSet(resultSet).get(0);
        } catch (SQLException e) {
            throw new RuntimeException("getByFeedId error", e);
        }
    }

    @Override
    public List<Content> getByFeedId(int feedId) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(
                    "SELECT * FROM content WHERE feed_id=?");
            preparedStatement.setInt(1, feedId);
            ResultSet resultSet = preparedStatement.executeQuery();
            return createContentFromResultSet(resultSet);
        } catch (SQLException e) {
            throw new RuntimeException("getByFeedId error", e);
        }
    }

    @Override
    public Content save(Content content) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(
                    "INSERT INTO content(type, relation, mode, value, feed_id) VALUES(?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, content.getSyndContent().getType());
            preparedStatement.setString(2, content.getRelation());
            preparedStatement.setString(3, content.getSyndContent().getMode());
            preparedStatement.setString(4, content.getSyndContent().getValue());
            preparedStatement.setInt(5, content.getFeed_id());
            int newId = preparedStatement.executeUpdate();
            content.setId(newId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return content;
    }
}
