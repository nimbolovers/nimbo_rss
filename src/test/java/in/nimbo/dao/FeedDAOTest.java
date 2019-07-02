package in.nimbo.dao;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import in.nimbo.entity.Entry;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Date;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DAO.class)
public class FeedDAOTest
{
    private ContentDAO contentDAO;
    private static Connection connection;

    private static String readFile(String path) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(path));
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("couldn't read file: " + path, e);
        }
    }

    @BeforeClass
    public static void init() throws SQLException, FileNotFoundException {
        connection = DriverManager.getConnection("jdbc:h2:~/h2_rss", "user", "");
        String queries = readFile("db/db_tables_sql.sql");
        PreparedStatement statement = connection.prepareStatement(queries);
        statement.execute();
    }

    @Before
    public void initTables() throws SQLException {
        PowerMockito.mockStatic(DAO.class);
        PowerMockito.when(DAO.getConnection()).thenReturn(connection);
        PreparedStatement statement = connection.prepareStatement("DELETE FROM content; " +
                "DELETE FROM feed");
        statement.executeUpdate();
    }

    @Test
    public void save() throws SQLException {
        contentDAO = PowerMockito.mock(ContentDAO.class);
        assertEquals(DAO.getConnection(), connection);
        FeedDAO feedDAO = new FeedDAOImpl(contentDAO);
        Entry entry = new Entry();
        SyndEntry syndEntry = new SyndEntryImpl();
        syndEntry.setPublishedDate(new Date());
        entry.setSyndEntry(syndEntry);
        if (!feedDAO.contain(entry)) {
            feedDAO.save(entry);
        }
        PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) as cnt FROM feed");
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()){
            int cnt = resultSet.getInt("cnt");
            assertTrue(cnt > 0);
        }
        else {
            fail();
        }
    }

}
