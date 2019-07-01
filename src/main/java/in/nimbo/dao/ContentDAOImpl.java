package in.nimbo.dao;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import in.nimbo.entity.Entry;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ContentDAOImpl extends DAO implements ContentDAO {

    @Override
    public SyndContent get(int id) {
        return null;
    }

    @Override
    public SyndContent getByFeedId(int feedId) {
        return null;
    }

    @Override
    public SyndContent save(SyndContent content) {
        return null;
    }
}
