package in.nimbo.dao;

import in.nimbo.application.Utility;
import in.nimbo.dao.pool.ConnectionPool;
import in.nimbo.dao.pool.ConnectionWrapper;
import in.nimbo.entity.*;
import in.nimbo.entity.report.HourReport;
import in.nimbo.entity.report.DateReport;
import in.nimbo.exception.QueryException;
import in.nimbo.exception.RecordNotFoundException;
import in.nimbo.exception.ResultSetFetchException;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.SelectConditionStep;
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
     * find entries which their content contain something in range of date
     *
     * @param channel channel which search for entry in it
     *                if it is not specified (null), then search in all channels
     * @param contentValue content value want to be in content of entry
     * @param titleValue title value want to be in title of entry
     * @param startDate start date of fetched data
     *                  if it is not specified (null), then there is no limitation on start time
     * @param finishDate finish date of fetched data
     *                   if it is not specified (null), then there is no limitation on finish time
     * @return list of entries which their content contain value and their title contain a value
     * @throws QueryException if it is unable to execute query
     */
    public List<Entry> filterEntry(String channel, String contentValue, String titleValue
            , Date startDate, Date finishDate) {
        try (ConnectionWrapper connection = ConnectionPool.getConnection()) {
            SelectConditionStep<Record> query = DSL.using(SQLDialect.MYSQL)
                    .select()
                    .from("feed")
                    .innerJoin("content").on(DSL.field("feed.id").eq(DSL.field("content.feed_id")))
                    .where(
                            DSL.field("content.value").like("%" + contentValue + "%")
                                    .and(DSL.field("title").like("%" + titleValue + "%"))
                    );
            if (channel != null && !channel.isEmpty())
                query = query.and(DSL.field("feed.channel").eq(channel));
            if (startDate != null)
                query = query.and(DSL.field("feed.pub_date").ge(new java.sql.Timestamp(startDate.getTime())));
            if (finishDate != null)
                query = query.and(DSL.field("feed.pub_date").le(new java.sql.Timestamp(finishDate.getTime())));

            String sqlQuery = query.getQuery().toString();
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
     * count of news for each hour for each site
     * @param title string which must appeared in the title (optional)
     * @return list of HourReport
     */
    @Override
    public List<HourReport> getHourReports(String title) {
        try (ConnectionWrapper connection = ConnectionPool.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    " SELECT channel, Hour(pub_date) as hour, COUNT(*) as cnt" +
                            " FROM feed" +
                            " WHERE pub_date IS NOT NULL" +
                            " and title LIKE ?" +
                            " GROUP BY channel, hour");
            statement.setString(1, "%" + (title != null ? title : "") + "%");
            ResultSet resultSet = statement.executeQuery();
            List<HourReport> reports = new ArrayList<>();
            while (resultSet.next()){
                String channel = resultSet.getString("channel");
                int hour = resultSet.getInt("hour");
                int cnt = resultSet.getInt("cnt");
                HourReport hourReport = new HourReport(channel, cnt, hour);
                reports.add(hourReport);
            }
            return reports;
        } catch (SQLException e) {
            logger.error("Unable to execute query: " + e.getMessage(), e);
            throw new QueryException("Unable to execute query", e);
        }
    }

    /**
     * count of news for each day for each site
     * @param title string which must appeared in the title (optional)
     * @param limit max number of results
     * @return sorted list of HourReport by year and month and day
     */
    @Override
    public List<DateReport> getDateReports(String title, int limit) {
        try (ConnectionWrapper connection = ConnectionPool.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT" +
                    " Year(pub_date) AS year, Month(pub_date) AS month, Day(pub_date) AS day, channel, COUNT(*) as cnt" +
                    " FROM feed" +
                    " WHERE title LIKE ? " +
                    " GROUP BY year, month, day, channel" +
                    " ORDER BY year DESC,month DESC,day DESC LIMIT ?");
            preparedStatement.setString(1, "%" +  (title != null ? title : "") + "%");
            preparedStatement.setInt(2, limit);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<DateReport> reports = new ArrayList<>();
            while (resultSet.next()){
                int year = resultSet.getInt("year");
                int month = resultSet.getInt("month");
                int day = resultSet.getInt("day");
                int count = resultSet.getInt("cnt");
                String channel = resultSet.getString("channel");
                DateReport report = new DateReport(channel, count, Utility.createDate(year, month, day));
                reports.add(report);
            }
            return reports;
        } catch (SQLException e) {
            logger.error("Unable to execute query: " + e.getMessage(), e);
            throw new QueryException("Unable to execute query", e);
        }


    }
}
