package in.nimbo.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import in.nimbo.dao.FeedDAO;
import in.nimbo.entity.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FeedService {
    private FeedDAO feedDAO;
    private Logger logger = LoggerFactory.getLogger(FeedService.class);

    public FeedService(FeedDAO feedDAO) {
        this.feedDAO = feedDAO;
    }

    public List<Entry> getFeeds() {
        return feedDAO.getEntries();
    }

    public List<Entry> getFeeds(String title) {
        return feedDAO.getEntryByTitle(title);
    }

    public List<Entry> save(String url) throws IOException, FeedException {
        logger.info("saving data from " + url);
        // use Rome library to parse url and get RSS
        URL url1 = new URL(url);
        logger.info("data fetched");
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(url1));
        logger.info("data parsed");
        List<Entry> list = new ArrayList<>(feed.getEntries().size());
        logger.info("start saving data");
        // add rss data to database
        int count = 0;
        for (SyndEntry entry : feed.getEntries()) {
            Entry newEntry = new Entry(feed.getTitle(), entry);
            if (!feedDAO.contain(newEntry)) {
                list.add(feedDAO.save(newEntry));
                count++;
            }
            else {
                logger.warn("data contains before. " + entry.getTitle());
            }
        }
        logger.info("saving data ended " + count + " data added");
        return list;
    }
}
