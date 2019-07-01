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
    private FeedDAO dao;

    public FeedService(FeedDAO dao) {
        this.dao = dao;
    }

    public List<Entry> getFeeds(){
        return dao.getFeeds();
    }

    public List<Entry> getFeeds(String title){
        return dao.filterFeeds(title);
    }

    public List<Entry> save(String url) throws IOException, FeedException {
        URL url1 = new URL(url);
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(url1));
        List<Entry> list = new ArrayList<>(feed.getEntries().size());
        for (SyndEntry entry:feed.getEntries()) {
            list.add(dao.save(new Entry(feed.getTitle(),entry)));
        }
        return list;
    }
}
