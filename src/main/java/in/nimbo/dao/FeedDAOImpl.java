package in.nimbo.dao;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import in.nimbo.entity.Content;
import in.nimbo.entity.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FeedDAOImpl extends DAO implements FeedDAO {
    private Logger logger = LoggerFactory.getLogger(FeedDAOImpl.class);
    private DescriptionDAO descriptionDAO;

    public FeedDAOImpl(DescriptionDAO descriptionDAO) {
        this.descriptionDAO = descriptionDAO;
    }

    /**
     * create a list of entries from a ResultSet of JDBC
     * @param resultSet resultSet of database
     * @return list of entries
     */
    private List<Entry> createEntryFromResultSet(ResultSet resultSet) {
        List<Entry> result = new ArrayList<>();
        try {
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

                List<Content> contents = descriptionDAO.getByFeedId(entry.getId());
                // fetch description
                Optional<Content> description = contents.stream()
                        .filter(content -> content.getRelation().equals("description"))
                        .findFirst();
                description.ifPresent(content -> syndEntry.setDescription(content.getSyndContent()));

                // fetch contents
                List<SyndContent> media = contents.stream()
                        .filter(content -> content.getRelation().equals("media"))
                        .map(Content::getSyndContent)
                        .collect(Collectors.toList());
                syndEntry.setContents(media);

                // fetch publication data
                syndEntry.setPublishedDate(resultSet.getDate(4));

                result.add(entry);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Unable to fetch data from ResultSet: " + e.getMessage());
            throw new RuntimeException("Unable to fetch data from ResultSet", e);
        }
    }

    /**
     * find entries which their title contain a "searchString" strings
     * @param searchString string want to be in title of entry
     * @return list of entries which their title contain "searchString"
     */
    @Override
    public List<Entry> getEntryByTitle(String searchString) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM feed WHERE title LIKE ?");
            preparedStatement.setString(1, "%" + searchString + "%");
            ResultSet resultSet = preparedStatement.executeQuery();
            return createEntryFromResultSet(resultSet);
        } catch (SQLException e) {
            logger.error("Unable to execute query: " + e.getMessage());
            throw new RuntimeException("Unable to fetch data from ResultSet", e);
        }
    }

    /**
     * fetch all of entries in database
     * @return a list of entries
     */
    @Override
    public List<Entry> getEntries() {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM feed");
            ResultSet resultSet = preparedStatement.executeQuery();
            return createEntryFromResultSet(resultSet);
        } catch (SQLException e) {
            logger.error("Unable to execute query: " + e.getMessage());
            throw new RuntimeException("Unable to execute query", e);
        }
    }

    /**
     * save an entry in database
     * contents of entry will be added to 'content' database
     *      description of entry will be added as a content of type 'description'
     *      contents of entry will be added as a content of type 'content'
     * @param entry entry
     * @return entry which it's ID will be set after adding to database
     */
    @Override
    public Entry save(Entry entry) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(
                    "INSERT INTO feed(channel, title, pub_date) VALUES(?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, entry.getChannel());
            preparedStatement.setString(2, entry.getSyndEntry().getTitle());
            preparedStatement.setDate(3, new Date(entry.getSyndEntry().getPublishedDate().getTime()));
            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            generatedKeys.next();
            int newId = generatedKeys.getInt(1);
            entry.setId(newId);

            // add entry description
            if (entry.getSyndEntry().getDescription() != null) {
                Content content = new Content("description", entry.getSyndEntry().getDescription());
                content.setFeed_id(newId);
                descriptionDAO.save(content);
            }

            // add entry contents
            if (!entry.getSyndEntry().getContents().isEmpty()) {
                for (SyndContent syndContent : entry.getSyndEntry().getContents()) {
                    Content content = new Content("content", syndContent);
                    content.setFeed_id(newId);
                    descriptionDAO.save(content);
                }
            }
        } catch (SQLException e) {
            logger.error("Unable to save entry with id=" + entry.getId() + ": " + e.getMessage());
            throw new RuntimeException("Unable to save entry with id=" + entry.getId(), e);
        }
        return entry;
    }

    /**
     * check whether database contain a same entry
     * check based on entry.title and entry.channel
     * @param entry which is checked
     * @return true if database contain same entry as given entry
     */
    @Override
    public boolean contain(Entry entry) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(
                    "SELECT COUNT(*) FROM feed WHERE channel=? AND title=?");
            preparedStatement.setString(1, entry.getChannel());
            preparedStatement.setString(2, entry.getSyndEntry().getTitle());
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) > 0;
        } catch (SQLException e) {
            logger.error("Unable to execute query: " + e.getMessage());
            throw new RuntimeException("Unable to execute query", e);
        }
    }
}
