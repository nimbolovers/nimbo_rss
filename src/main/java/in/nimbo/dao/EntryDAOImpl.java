package in.nimbo.dao;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import in.nimbo.entity.Content;
import in.nimbo.entity.Description;
import in.nimbo.entity.Entry;
import in.nimbo.exception.RecordNotFoundException;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.SelectConditionStep;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EntryDAOImpl extends DAO implements EntryDAO {
    private Logger logger = LoggerFactory.getLogger(EntryDAOImpl.class);

    private DescriptionDAO descriptionDAO;
    private ContentDAO contentDAO;

    public EntryDAOImpl(DescriptionDAO descriptionDAO, ContentDAO contentDAO) {
        this.descriptionDAO = descriptionDAO;
        this.contentDAO = contentDAO;
    }

    /**
     * create a list of entries from a ResultSet of JDBC
     *
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

                // fetch description
                try {
                    Description description = descriptionDAO.getByFeedId(entry.getId());
                    syndEntry.setDescription(description.getSyndContent());
                } catch (RecordNotFoundException e) {
                   // doesn't set description and ignore error
                }

                // fetch descriptions
                Content content = contentDAO.getByFeedId(entry.getId());
                entry.setContent(content.getValue());

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
     * find entries which their title contain something in range of date
     *
     * @param channel channel which search for entry in it
     *                if it is not specified (null), then search in all channels
     * @param value value want to be in title of entry
     * @param startDate start date of fetched data
     *                  if it is not specified (null), then there is no limitation on start time
     * @param finishDate finish date of fetched data
     *                   if it is not specified (null), then there is no limitation on finish time
     * @return list of entries which their title contain value
     * @throws RuntimeException if it is unable to execute query
     */
    @Override
    public List<Entry> filterEntryByTitle(String channel, String value, Date startDate, Date finishDate) {
        try {
            SelectConditionStep<Record> query = DSL.using(SQLDialect.MYSQL)
                    .select()
                    .from("feed")
                    .where(DSL.field("title").like("%" + value + "%"));
            if (channel != null)
                query = query.and(DSL.field("channel").eq(channel));
            if (startDate != null)
                query = query.and(DSL.field("pub_date").ge(startDate));
            if (finishDate != null)
                query = query.and(DSL.field("pub_date").le(finishDate));

            String sqlQuery = query.getSQL(ParamType.INLINED);
            Statement statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            return createEntryFromResultSet(resultSet);
        } catch (SQLException e) {
            logger.error("Unable to execute query: " + e.getMessage());
            throw new RuntimeException("Unable to fetch data from ResultSet", e);
        }
    }

    /**
     * find entries which their content contain something in range of date
     *
     * @param channel channel which search for entry in it
     *                if it is not specified (null), then search in all channels
     * @param value value want to be in content of entry
     * @param startDate start date of fetched data
     *                  if it is not specified (null), then there is no limitation on start time
     * @param finishDate finish date of fetched data
     *                   if it is not specified (null), then there is no limitation on finish time
     * @return list of entries which their content contain value
     * @throws RuntimeException if it is unable to execute query
     */
    @Override
    public List<Entry> filterEntryByContent(String channel, String value, Date startDate, Date finishDate) {
        try {
            SelectConditionStep<Record> query = DSL.using(SQLDialect.MYSQL)
                    .select()
                    .from("feed")
                    .innerJoin("content").on(DSL.field("feed.id").eq(DSL.field("content.feed_id")))
                    .where(DSL.field("content.value").like("%" + value + "%"));
            if (channel != null)
                query = query.and(DSL.field("feed.channel").eq(channel));
            if (startDate != null)
                query = query.and(DSL.field("feed.pub_date").ge(startDate));
            if (finishDate != null)
                query = query.and(DSL.field("feed.pub_date").le(finishDate));

            String sqlQuery = query.getSQL(ParamType.INLINED);
            Statement statement = getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            return createEntryFromResultSet(resultSet);
        } catch (SQLException e) {
            logger.error("Unable to execute query: " + e.getMessage());
            throw new RuntimeException("Unable to fetch data from ResultSet", e);
        }
    }

    /**
     * fetch all of entries in database
     *
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
     * description of entry will be added as a content of type 'description'
     * contents of entry will be added as a content of type 'content'
     *
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
            preparedStatement.setDate(3, new java.sql.Date(entry.getSyndEntry().getPublishedDate().getTime()));
            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            generatedKeys.next();
            int newId = generatedKeys.getInt(1);
            entry.setId(newId);

            // add entry description
            if (entry.getSyndEntry().getDescription() != null) {
                Description description = new Description(entry.getSyndEntry().getDescription());
                description.setFeed_id(newId);
                descriptionDAO.save(description);
            }

            // add entry contents
            Content content = new Content(entry.getContent(), newId);
            contentDAO.save(content);

        } catch (SQLException e) {
            logger.error("Unable to save entry with id=" + entry.getId() + ": " + e.getMessage());
            throw new RuntimeException("Unable to save entry with id=" + entry.getId(), e);
        }
        return entry;
    }

    /**
     * check whether database contain a same entry
     * check based on entry.title and entry.channel
     *
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
