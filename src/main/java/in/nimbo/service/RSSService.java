package in.nimbo.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import in.nimbo.dao.EntryDAO;
import in.nimbo.entity.Entry;
import in.nimbo.entity.Site;
import in.nimbo.exception.ContentExtractingException;
import net.dankito.readability4j.Article;
import net.dankito.readability4j.Readability4J;
import org.jsoup.Jsoup;
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

    public List<Entry> filterEntryByTitle(String channel, String title, Date startTime, Date finishTime) {
        return entryDAO.filterEntryByTitle(channel, title, startTime, finishTime);
    }

    public List<Entry> filterEntryByContent(String channel, String content, Date startTime, Date finishTime) {
        return entryDAO.filterEntryByContent(channel, content, startTime, finishTime);
    }

    /**
     * add all of new entries of site to database
     *  if unable to get content of one site, add it to database with empty content
     * @param site site of feed
     * @param feed contain all feeds of site RSS url
     * @return list of all new entries which saved in database
     *
     */
    public List<Entry> addSiteEntries(Site site, SyndFeed feed) {
        List<Entry> newEntries = new ArrayList<>();
        for (SyndEntry syndEntry : feed.getEntries()) {
            Entry entry = new Entry(feed.getTitle(), syndEntry);
            if (!entryDAO.contain(entry)) {
                // fetch content of entry from link and save it in database
                // if unable to get one of entry, ignore it
                String contentOfRSSLink = "";
                try {
                    contentOfRSSLink = getContentOfRSSLink(syndEntry.getLink());
                } catch (ContentExtractingException e) {
                    logger.warn("Unable to extract content (ignored): " + syndEntry.getLink());
                }
                entry.setContent(contentOfRSSLink);

                entryDAO.save(entry);
                newEntries.add(entry);
            }
        }
        if (newEntries.size() == feed.getEntries().size()) {
            logger.info("Add " + newEntries.size() + " entries from: " + site.getLink());
        } else if (newEntries.isEmpty()) {
            logger.warn("There is no new entry from: " + site.getLink());
        } else {
            logger.info("Add " + newEntries.size() + "/" + feed.getEntries().size() + " entries from: " + site.getLink());
        }

        site.increaseNewsCount(newEntries.size());
        return newEntries;
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
            String html = Jsoup.connect(rssURL.toString()).get().html();
            Readability4J readability4J = new Readability4J("", html); // url is just needed to resolve relative urls
            Article article = readability4J.parse();
            return article.getContent();
        } catch (IOException e) {
            throw new ContentExtractingException("Unable to extract html content from rss link", e);
        }
    }

    /**
     * Fetch an SyndFeed from RSS URL
     * @param url url which is an RSS
     * @return SyndFeed which contain RSS contents
     * @throws RuntimeException if RSS url link is not valid or unable to fetch data from url
     */
    public SyndFeed fetchFromURL(String url) {
        try {
            URL encodedURL = Utility.encodeURL(url);
            logger.info("Fetch data of RSS from URL: " + url);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(encodedURL));
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
}
