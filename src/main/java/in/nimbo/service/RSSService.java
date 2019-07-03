package in.nimbo.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import in.nimbo.dao.EntryDAO;
import in.nimbo.entity.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RSSService {
    private EntryDAO entryDAO;
    private Logger logger = LoggerFactory.getLogger(RSSService.class);

    public RSSService(EntryDAO entryDAO) {
        this.entryDAO = entryDAO;
    }

    public List<Entry> filterEntryByTitle() {
        return entryDAO.getEntries();
    }

    public List<Entry> filterEntryByTitle(String channel, String title, Date startTime, Date finishTime) {
        return entryDAO.filterEntryByTitle(channel, title, startTime, finishTime);
    }

    public List<Entry> filterEntryByContent(String channel, String content, Date startTime, Date finishTime) {
        return entryDAO.filterEntryByContent(channel, content, startTime, finishTime);
    }

    public List<Entry> save(SyndFeed feed) {
        int count = 0;
        List<Entry> resultEntries = new ArrayList<>();
        for (SyndEntry syndEntry : feed.getEntries()) {
            Entry entry = new Entry(feed.getTitle(), syndEntry);
            entry.setContent(getContentOfRSSLink(syndEntry.getLink()));
            if (!entryDAO.contain(entry)) {
                entryDAO.save(entry);
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



    /**
     * Fetch an SyndFeed from RSS URL
     * @param url url which is an RSS
     * @return SyndFeed which contain RSS contents
     */
    public SyndFeed fetchFromURL(String url) {
        try {
            logger.info("Fetch data of RSS from URL: " + url);
            URL url1 = Utility.encodeURL(url);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(url1));
            logger.info("RSS data fetched successfully from: "+ url);
            return feed;
        } catch (FeedException e) {
            logger.error("Invalid RSS URL: " + url);
            throw new RuntimeException("Invalid RSS URL: " + url, e);
        } catch (MalformedURLException e) {
            logger.error("Illegal URL format: " + url);
            throw new RuntimeException("Illegal URL format", e);
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
            URL rssURL = Utility.encodeURL(link);
            return ArticleExtractor.INSTANCE.getText(rssURL);
        } catch (BoilerpipeProcessingException e) {
            logger.error("Unable to extract content from " + link);
            throw new RuntimeException("Unable to extract content from rss link", e);
        }
    }
}
