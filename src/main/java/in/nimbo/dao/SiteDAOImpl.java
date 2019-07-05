package in.nimbo.dao;

import in.nimbo.entity.Site;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SiteDAOImpl extends DAO implements SiteDAO {
    private Logger logger = LoggerFactory.getLogger(SiteDAOImpl.class);

    /**
     * create a list of sites from a ResultSet of JDBC
     *
     * @param resultSet resultSet of database
     * @return list of sites
     * @throws RuntimeException if unable to fetch data from ResultSet
     */
    private List<Site> createSiteFromResultSet(ResultSet resultSet) {
        List<Site> sites = new ArrayList<>();
        try {
            while (resultSet.next()) {
                Site site = new Site();

                // fetch id
                site.setId(resultSet.getInt(1));

                // fetch name
                site.setName(resultSet.getString(2));

                // fetch link
                site.setLink(resultSet.getString(3));

                sites.add(site);
            }
        } catch (SQLException e) {
            logger.error("Unable to fetch data from ResultSet: " + e.getMessage());
            throw new RuntimeException("Unable to fetch data from ResultSet", e);
        }
        return sites;
    }

    /**
     * fetch all of sites in database
     *
     * @return a list of sites
     */
    @Override
    public List<Site> getSites() {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM site");
            ResultSet resultSet = preparedStatement.executeQuery();
            return createSiteFromResultSet(resultSet);
        } catch (SQLException e) {
            logger.error("Unable to execute query: " + e.getMessage());
            throw new RuntimeException("Unable to execute query", e);
        }
    }

    /**
     * save an site in database
     *
     * @param site site which is saved
     * @return site which it's ID will be set after adding to database
     */
    @Override
    public Site save(Site site) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(
                    "INSERT INTO site(name, link) VALUES(?, ?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, site.getName());
            preparedStatement.setString(2, site.getLink());

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            generatedKeys.next();
            int newId = generatedKeys.getInt(1);
            site.setId(newId);
        } catch (SQLException e) {
            logger.error("Unable to execute query: " + e.getMessage());
            throw new RuntimeException("Unable to execute query", e);
        }
        return site;
    }
}
