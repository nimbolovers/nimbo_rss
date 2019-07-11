package in.nimbo.dao;

import in.nimbo.dao.pool.ConnectionPool;
import in.nimbo.entity.Content;
import in.nimbo.entity.Description;
import in.nimbo.entity.Entry;
import in.nimbo.entity.report.DateReport;
import in.nimbo.entity.report.HourReport;
import in.nimbo.entity.report.Report;
import in.nimbo.exception.QueryException;
import org.apache.commons.dbutils.DbUtils;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EntryDAOImpl implements EntryDAO {
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
     * @throws SQLException if unable to fetch data from ResultSet
     */
    private List<Entry> createEntryFromResultSet(ResultSet resultSet) throws SQLException {
        List<Entry> result = new ArrayList<>();
        while (resultSet.next()) {
            Entry entry = new Entry();
            entry.setId(resultSet.getInt("id"));
            entry.setChannel(resultSet.getString("channel"));
            entry.setTitle(resultSet.getString("title"));
            Optional<Description> description = descriptionDAO.getByFeedId(entry.getId());
            description.ifPresent(entry::setDescription);
            Optional<Content> content = contentDAO.getByFeedId(entry.getId());
            content.ifPresent(value -> entry.setContent(value.getValue()));
            entry.setLink(resultSet.getString("link"));
            entry.setPublicationDate(resultSet.getObject("pub_date", LocalDateTime.class));
            result.add(entry);
        }
        return result;
    }

    /**
     * find entries which their content contain something in range of date
     *
     * @param channel      channel which search for entry in it
     *                     if it is not specified (null), then search in all channels
     * @param contentValue content value want to be in content of entry
     * @param titleValue   title value want to be in title of entry
     * @param startDate    start date of fetched data
     *                     if it is not specified (null), then there is no limitation on start time
     * @param finishDate   finish date of fetched data
     *                     if it is not specified (null), then there is no limitation on finish time
     * @return list of entries which their content contain value and their title contain a value
     * @throws QueryException if it is unable to execute query
     */
    public List<Entry> filterEntry(String channel, String contentValue, String titleValue
            , LocalDateTime startDate, LocalDateTime finishDate) {
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
            query = query.and(DSL.field("feed.pub_date").ge(startDate));
        if (finishDate != null)
            query = query.and(DSL.field("feed.pub_date").le(finishDate));

        String sqlQuery = query.getQuery().toString();

        try (Connection connection = ConnectionPool.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sqlQuery)) {
            return createEntryFromResultSet(resultSet);
        } catch (SQLException e) {
            throw new QueryException(e);
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
        try (Connection connection = ConnectionPool.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM feed");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            return createEntryFromResultSet(resultSet);
        } catch (SQLException e) {
            throw new QueryException(e);
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
        ResultSet generatedKeys = null;
        try (Connection connection = ConnectionPool.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO feed(channel, title, pub_date, link) VALUES(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);) {
            preparedStatement.setString(1, entry.getChannel());
            preparedStatement.setString(2, entry.getTitle());
            preparedStatement.setObject(3, entry.getPublicationDate());
            preparedStatement.setString(4, entry.getLink());
            preparedStatement.executeUpdate();
            generatedKeys = preparedStatement.getGeneratedKeys();
            generatedKeys.next();
            int newId = generatedKeys.getInt(1);
            entry.setId(newId);

            // add entry description
            if (entry.getDescription() != null) {
                entry.getDescription().setFeedId(newId);
                descriptionDAO.save(entry.getDescription());
            }

            // add entry contents
            Content content = new Content(entry.getContent(), newId);
            contentDAO.save(content);

        } catch (SQLException e) {
            throw new QueryException(e);
        } finally {
            DbUtils.closeQuietly(generatedKeys);
        }
        return entry;
    }

    /**
     * check whether an entry exists in database based on entry.title and entry.channel
     *
     * @param entry which is checked
     * @return true if entry exists in database
     * @throws QueryException if unable to execute query
     */
    @Override
    public boolean contain(Entry entry) {
        ResultSet resultSet = null;
        try (Connection connection = ConnectionPool.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM feed WHERE link=?")) {
            preparedStatement.setString(1, entry.getLink());
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) > 0;
        } catch (SQLException e) {
            throw new QueryException(e);
        } finally {
            DbUtils.closeQuietly(resultSet);
        }
    }

    /**
     * count of news for each hour for each site
     *
     * @param title string which must appeared in the title (optional)
     * @return list of HourReport
     */
    @Override
    public List<HourReport> getHourReports(String title, String channel) {
        ResultSet resultSet = null;
        try (Connection connection = ConnectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     " SELECT channel AS groupChannel, Hour(pub_date) as hour, COUNT(*) as cnt" +
                             " FROM feed" +
                             " WHERE pub_date IS NOT NULL" +
                             " and title LIKE ?" +
                             " and channel LIKE ?" +
                             " GROUP BY groupChannel, hour")) {
            statement.setString(1, "%" + (title != null ? title : "") + "%");
            statement.setString(2, "%" + (channel != null ? channel : "") + "%");
            resultSet = statement.executeQuery();
            List<HourReport> reports = new ArrayList<>();
            while (resultSet.next()) {
                HourReport hourReport = new HourReport(
                        resultSet.getString("groupChannel"),
                        resultSet.getInt("cnt"),
                        resultSet.getInt("hour"));
                reports.add(hourReport);
            }
            return reports;
        } catch (SQLException e) {
            throw new QueryException(e);
        } finally {
            DbUtils.closeQuietly(resultSet);
        }
    }

    /**
     * count of news for each day for each site
     *
     * @param title string which must appeared in the title (optional)
     * @param limit max number of results
     * @return sorted list of HourReport by year and month and day
     */
    @Override
    public List<DateReport> getDateReports(String title, int limit) {
        ResultSet resultSet = null;
        try (Connection connection = ConnectionPool.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT" +
                     " Year(pub_date) AS year, Month(pub_date) AS month, Day(pub_date) AS day, channel AS baseChannel, COUNT(*) AS cnt" +
                     " FROM feed" +
                     " WHERE title LIKE ? AND pub_date IS NOT NULL" +
                     " GROUP BY year, month, day, baseChannel" +
                     " ORDER BY year DESC, month DESC, day DESC LIMIT ?");) {
            preparedStatement.setString(1, "%" + (title != null ? title : "") + "%");
            preparedStatement.setInt(2, limit);
            resultSet = preparedStatement.executeQuery();
            List<DateReport> reports = new ArrayList<>();
            while (resultSet.next()) {
                DateReport report = new DateReport(resultSet.getString("baseChannel"), resultSet.getInt("cnt"),
                        LocalDateTime.of(
                                resultSet.getInt("year"),
                                resultSet.getInt("month"),
                                resultSet.getInt("day"), 0, 0));
                reports.add(report);
            }
            return reports;
        } catch (SQLException e) {
            throw new QueryException(e);
        } finally {
            DbUtils.closeQuietly(resultSet);
        }
    }

    @Override
    public List<Report> getAllReports(String title, LocalDateTime date) {
        ResultSet resultSet = null;
        try (Connection connection = ConnectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) AS cnt, channel " +
                             "FROM feed " +
                             "WHERE title LIKE ? " +
                             (date != null ? "AND pub_date BETWEEN ? AND ? " : "") +
                             "GROUP BY channel")) {
            statement.setString(1, "%" + (title != null ? title : "") + "%");
            if (date != null) {
                statement.setObject(2, date);
                statement.setObject(3, LocalDateTime.from(date).plusDays(1));
            }
            resultSet = statement.executeQuery();
            List<Report> reports = new ArrayList<>();
            while (resultSet.next()) {
                int cnt = resultSet.getInt("cnt");
                String channel = resultSet.getString("channel");
                Report report = new Report(channel, cnt);
                reports.add(report);
            }
            return reports;
        } catch (SQLException e) {
            throw new QueryException(e);
        } finally {
            DbUtils.closeQuietly(resultSet);
        }
    }
}
