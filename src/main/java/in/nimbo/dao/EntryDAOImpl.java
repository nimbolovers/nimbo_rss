package in.nimbo.dao;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import in.nimbo.application.Utility;
import in.nimbo.dao.pool.ConnectionPool;
import in.nimbo.dao.pool.ConnectionWrapper;
import in.nimbo.entity.Content;
import in.nimbo.entity.Description;
import in.nimbo.entity.Entry;
import in.nimbo.entity.SiteReport;
import in.nimbo.exception.QueryException;
import in.nimbo.exception.RecordNotFoundException;
import in.nimbo.exception.ResultSetFetchException;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.SelectConditionStep;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EntryDAOImpl implements EntryDAO {
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
     * @throws ResultSetFetchException if unable to fetch data from ResultSet
     */
    private List<Entry> createEntryFromResultSet(ResultSet resultSet) {
        List<Entry> result = new ArrayList<>();
        try {
            while (resultSet.next()) {
                Entry entry = new Entry();

                // fetch id
                entry.setId(resultSet.getInt("id"));

                // fetch channel
                entry.setChannel(resultSet.getString("channel"));

                // fetch title
                entry.setTitle(resultSet.getString("title"));

                // fetch description
                try {
                    Description description = descriptionDAO.getByFeedId(entry.getId());
                    entry.setDescription(description);
                } catch (RecordNotFoundException e) {
                   // doesn't set description and ignore error
                }

                // fetch content
                Content content = contentDAO.getByFeedId(entry.getId());
                entry.setContent(content.getValue());

                // fetch link
                entry.setLink(resultSet.getString("link"));

                // fetch publication data
                entry.setPublicationDate(resultSet.getTimestamp("pub_date"));

                result.add(entry);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Unable to fetch data from ResultSet: " + e.getMessage(), e);
            throw new ResultSetFetchException("Unable to fetch data from ResultSet", e);
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
     * @throws QueryException if it is unable to execute query
     */
    @Override
    public List<Entry> filterEntryByTitle(String channel, String value, Date startDate, Date finishDate) {
        try (ConnectionWrapper connection = ConnectionPool.getConnection()) {
            SelectConditionStep<Record> query = DSL.using(SQLDialect.MYSQL)
                    .select()
                    .from("feed")
                    .where(DSL.field("title").like("%" + value + "%"));
            if (channel != null)
                query = query.and(DSL.field("channel").eq(channel));
            if (startDate != null)
                query = query.and(DSL.field("pub_date").ge(new java.sql.Timestamp(startDate.getTime())));
            if (finishDate != null)
                query = query.and(DSL.field("pub_date").le(new java.sql.Timestamp(finishDate.getTime())));

            String sqlQuery = query.getSQL(ParamType.INLINED);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            return createEntryFromResultSet(resultSet);
        } catch (SQLException e) {
            logger.error("Unable to execute query: " + e.getMessage(), e);
            throw new QueryException("Unable to execute query", e);
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
     * @throws QueryException if it is unable to execute query
     */
    @Override
    public List<Entry> filterEntryByContent(String channel, String value, Date startDate, Date finishDate) {
        try (ConnectionWrapper connection = ConnectionPool.getConnection()) {
            SelectConditionStep<Record> query = DSL.using(SQLDialect.MYSQL)
                    .select()
                    .from("feed")
                    .innerJoin("content").on(DSL.field("feed.id").eq(DSL.field("content.feed_id")))
                    .where(DSL.field("content.value").like("%" + value + "%"));
            if (channel != null)
                query = query.and(DSL.field("feed.channel").eq(channel));
            if (startDate != null)
                query = query.and(DSL.field("feed.pub_date").ge(new java.sql.Timestamp(startDate.getTime())));
            if (finishDate != null)
                query = query.and(DSL.field("feed.pub_date").le(new java.sql.Timestamp(finishDate.getTime())));

            String sqlQuery = query.getSQL(ParamType.INLINED);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            return createEntryFromResultSet(resultSet);
        } catch (SQLException e) {
            logger.error("Unable to execute query: " + e.getMessage(), e);
            throw new QueryException("Unable to execute query", e);
        }
    }

    /**
     * fetch all of entries in database
     *
     * @return a list of entries
     * @throws QueryException if unable to execute query
     */
    @Override
    public List<Entry> getEntries() {
        try (ConnectionWrapper connection = ConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM feed");
            ResultSet resultSet = preparedStatement.executeQuery();
            return createEntryFromResultSet(resultSet);
        } catch (SQLException e) {
            logger.error("Unable to execute query: " + e.getMessage(), e);
            throw new QueryException("Unable to execute query", e);
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
     * @throws QueryException if unable to execute query
     */
    @Override
    public Entry save(Entry entry) {
        try (ConnectionWrapper connection = ConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO feed(channel, title, pub_date, link) VALUES(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, entry.getChannel());
            preparedStatement.setString(2, entry.getTitle());
            if (entry.getPublicationDate() != null)
                preparedStatement.setTimestamp(3, new java.sql.Timestamp(entry.getPublicationDate().getTime()));
            else
                preparedStatement.setTimestamp(3, null);
            preparedStatement.setString(4, entry.getLink());
            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            generatedKeys.next();
            int newId = generatedKeys.getInt(1);
            entry.setId(newId);

            // add entry description
            if (entry.getDescription() != null) {
                entry.getDescription().setFeed_id(newId);
                descriptionDAO.save(entry.getDescription());
            }

            // add entry contents
            Content content = new Content(entry.getContent(), newId);
            contentDAO.save(content);

        } catch (SQLException e) {
            logger.error("Unable to save entry with id=" + entry.getId() + ": " + e.getMessage(), e);
            throw new QueryException("Unable to save entry with id=" + entry.getId(), e);
        }
        return entry;
    }

    /**
     * check whether an entry exists in database based on entry.title and entry.channel
     * @param entry which is checked
     * @return true if entry exists in database
     * @throws QueryException if unable to execute query
     */
    @Override
    public boolean contain(Entry entry) {
        try (ConnectionWrapper connection = ConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM feed WHERE link=?");
            preparedStatement.setString(1, entry.getLink());
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) > 0;
        } catch (SQLException e) {
            logger.error("Unable to execute query: " + e.getMessage(), e);
            throw new QueryException("Unable to execute query", e);
        }
    }

    /**
     * @param title filters entries that its title contains this title
     * @param limit max size of result
     * @return report which contain a site news count in each day
     */
    @Override
    public List<SiteReport> getSiteReports(String title, int limit) {
        try (ConnectionWrapper connection = ConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("select" +
                    " Year(pub_date) as year, Month(pub_date) as month, Day(pub_date) as day, channel, count(*) as cnt" +
                    " from feed" +
                    ((title != null) ? " where title like ? " : "")+
                    " group by year, month, day, channel" +
                    " order by year desc,month desc,day desc limit ?");
            if (title != null){
                preparedStatement.setString(1,"%" + title + "%");
            }
            preparedStatement.setInt(title != null ? 2 : 1, limit);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<SiteReport> reports = new ArrayList<>();
            while (resultSet.next()){
                int year = resultSet.getInt("year");
                int month = resultSet.getInt("month");
                int day = resultSet.getInt("day");
                int count = resultSet.getInt("cnt");
                String channel = resultSet.getString("channel");
                SiteReport report = new SiteReport();
                report.setChannel(channel);
                report.setDate(Utility.createDate(year, month, day));
                report.setCount(count);
                reports.add(report);
            }
            return reports;
        } catch (SQLException e) {
            logger.error("Unable to execute query: " + e.getMessage(), e);
            throw new QueryException("Unable to execute query", e);
        }
    }
}
