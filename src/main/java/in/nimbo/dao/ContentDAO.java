package in.nimbo.dao;

import com.rometools.rome.feed.synd.SyndContent;

public interface ContentDAO {
    SyndContent get(int id);

    SyndContent getByFeedId(int feedId);

    SyndContent save(SyndContent content);
}
