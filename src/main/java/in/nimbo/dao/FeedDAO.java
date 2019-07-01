package in.nimbo.dao;

import in.nimbo.entity.Entry;

import java.util.List;

public interface FeedDAO {
    List<Entry> filterFeeds(String title);
    List<Entry> getFeeds();
    Entry save(Entry entry);
}
