package in.nimbo.dao;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import in.nimbo.entity.Content;
import in.nimbo.entity.Entry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeedDAOImpl extends DAO implements FeedDAO {
    private ContentDAO contentDAO;

    public FeedDAOImpl(ContentDAO contentDAO) {
        this.contentDAO = contentDAO;
    }

    @Override
    public List<Entry> filterFeeds(String title) {
        return null;
    }

    @Override
    public List<Entry> getFeeds() {
        List<Entry> result = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM feed");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Entry entry = new Entry();
                SyndEntry syndEntry = new SyndEntryImpl();
                entry.setSyndEntry(syndEntry);

                // fetch id
                entry.setId(resultSet.getInt(1));

                // fetch channel
                entry.setChannel(resultSet.getString(2));

                // fetch title
                syndEntry.setTitle(resultSet.getString(3));

                // fetch description
                List<Content> contents = contentDAO.getByFeedId(entry.getId());
                Content description = contents.stream()
                        .filter(content -> content.getRelation().equals("description"))
                        .findFirst().get();
                syndEntry.setDescription(description.getSyndContent());

                // fetch publication data
                syndEntry.setPublishedDate(resultSet.getDate(4));

                result.add(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Entry save(Entry entry) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(
                    "INSERT INTO feed(channel, title, pub_date) VALUES(?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, entry.getChannel());
            preparedStatement.setString(2, entry.getSyndEntry().getTitle());
            preparedStatement.setDate(3, new Date(entry.getSyndEntry().getPublishedDate().getTime()));
            int newId = preparedStatement.executeUpdate();
            entry.setId(newId);

            Content content = new Content("description", entry.getSyndEntry().getDescription());
            content.setFeed_id(newId);
            contentDAO.save(content);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entry;
    }
}
