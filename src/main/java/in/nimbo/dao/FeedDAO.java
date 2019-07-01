package in.nimbo.dao;

import com.rometools.rome.feed.synd.SyndEntry;

import java.util.List;

public interface FeedDAO {
    List<SyndEntry> getFeeds(String title);
    List<SyndEntry> getFeeds();
    SyndEntry save(SyndEntry entry);
}
