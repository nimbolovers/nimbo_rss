package in.nimbo.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import in.nimbo.dao.FeedDAO;
import in.nimbo.entity.Entry;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FeedService {
    private FeedDAO feedDAO;

    public FeedService(FeedDAO feedDAO) {
        this.feedDAO = feedDAO;
    }

    public List<Entry> getFeeds() {
        return feedDAO.getFeeds();
    }

    public List<Entry> getFeeds(String title) {
        return feedDAO.filterFeeds(title);
    }

    public List<Entry> save(String url) throws IOException, FeedException {
        // use Rome library to parse url and get RSS
        URL url1 = new URL(url);
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(url1));
        List<Entry> list = new ArrayList<>(feed.getEntries().size());

        // add rss data to database
        for (SyndEntry entry : feed.getEntries()) {
            Entry newEntry = new Entry(feed.getTitle(), entry);
            if (!feedDAO.contain(newEntry))
                list.add(feedDAO.save(newEntry));
        }
        return list;
    }
}
