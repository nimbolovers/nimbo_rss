package in.nimbo.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import in.nimbo.dao.FeedDAO;
import in.nimbo.entity.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
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

    public List<Entry> save(SyndFeed feed) {
        int count = 0;
        List<Entry> resultEntries = new ArrayList<>();
        for (SyndEntry syndEntry : feed.getEntries()) {
            Entry entry = new Entry(feed.getTitle(), syndEntry);
            entry.setContent(getContentOfRSSLink(syndEntry.getLink()));
            if (!feedDAO.contain(entry)) {
                feedDAO.save(entry);
                resultEntries.add(entry);
                count++;
            }
        }
        if (count == feed.getEntries().size()) {
            logger.info("Add " + count + " entry to database");
        } else if (count == 0) {
            logger.warn("There is no new entry");
        } else {
            logger.info("Add " + count + "/" + feed.getEntries().size() + " entries");
        }
        return resultEntries;
    }

    public SyndFeed fetchFromURL(String url) {
        try {
            logger.info("fetching data from url: " + url);
            URL url1 = new URL(Utility.encodeURL(url));
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(url1));
            logger.info("URL fetched successfully");
            return feed;
        } catch (FeedException e) {
            logger.error("Invalid RSS");
            throw new RuntimeException("Invalid RSS", e);
        } catch (MalformedURLException e) {
            logger.error("URL not found: " + url);
            throw new RuntimeException("URL not found: " + url, e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * get article content of a site from a link using boilerpipe library
     *
     * @param link link of corresponding site
     * @return string which is main content of site
     * @throws RuntimeException if link is not a illegal URL or couldn't extract
     *                          main content from url
     */
    public String getContentOfRSSLink(String link) {
        try {
            URL rssURL = new URL(Utility.encodeURL(link));
            return ArticleExtractor.INSTANCE.getText(rssURL);
        } catch (MalformedURLException e) {
            logger.error("Unsupported URL format: " + link);
            throw new RuntimeException("Unsupported URL format", e);
        } catch (BoilerpipeProcessingException e) {
            logger.error("Unable to extract content from " + link);
            throw new RuntimeException("Unable to extract content from rss link", e);
        }
    }
}
