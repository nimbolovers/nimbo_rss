package in.nimbo.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import in.nimbo.application.Utility;
import in.nimbo.dao.EntryDAO;
import in.nimbo.dao.SiteDAO;
import in.nimbo.entity.Description;
import in.nimbo.entity.Entry;
import in.nimbo.entity.Site;
import in.nimbo.entity.report.DateReport;
import in.nimbo.entity.report.HourReport;
import in.nimbo.exception.*;
import net.dankito.readability4j.Article;
import net.dankito.readability4j.Readability4J;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class RSSService {
    private EntryDAO entryDAO;
    private SiteDAO siteDAO;
    private Logger logger = LoggerFactory.getLogger(RSSService.class);
    private static final int DAY_COUNT = 3;

    public RSSService(EntryDAO entryDAO, SiteDAO siteDAO) {
        this.entryDAO = entryDAO;
        this.siteDAO = siteDAO;
    }

    /**
     * update a site on DAO
     *
     * @param site site which it's id must be set
     */
    public void updateSite(Site site) throws RssServiceException {
        try {
            siteDAO.update(site);
        } catch (QueryException | IllegalArgumentException e) {
            throw new RssServiceException("Unable to update site: " + site.getName(), e);
        }
    }

    /**
     * @param channel      channel of entry
     *                     If it is null, ignore it
     * @param contentValue content of entry (required)
     * @param titleValue   title of entry (required)
     * @param startTime    is the lower bound for publication date of entries
     *                     If it is null, ignore it
     * @param finishTime   is the upper bound for publication date of entries
     *                     If it is null, ignore it
     * @return entries that accepted all filters
     * @throws RssServiceException if any exception happen during fetching data from DAO
     */
    public List<Entry> filterEntry(String channel, String contentValue, String titleValue, LocalDateTime startTime, LocalDateTime finishTime) throws RssServiceException {
        try {
            return entryDAO.filterEntry(channel, contentValue, titleValue, startTime, finishTime);
        } catch (QueryException | ResultSetFetchException | IllegalArgumentException e) {
            throw new RssServiceException(e.getMessage(), e);
        }
    }

    /**
     * add all of new entries of site to database
     * if unable to get content of one site, add it to database with empty content
     *
     * @param siteLink site of feed
     * @param entries  contain all entries of site
     * @return list of all new entries which saved in database
     */
    public List<Entry> addSiteEntries(String siteLink, List<Entry> entries) {
        List<Entry> newEntries = new ArrayList<>();
        for (Entry entry : entries) {
            if (!entryDAO.contain(entry)) {
                // fetch content of entry from link and save it in database
                // if unable to get one of entry, ignore it
                String contentOfRSSLink = "";
                try {
                    contentOfRSSLink = getContentOfRSSLink(entry.getLink());
                } catch (ContentExtractingException e) {
                    logger.warn(e.getMessage(), e);
                }
                entry.setContent(contentOfRSSLink);

                entryDAO.save(entry);
                newEntries.add(entry);
            }
        }
        if (newEntries.size() == entries.size()) {
            logger.info("Add " + newEntries.size() + " entries from: " + siteLink);
        } else if (!newEntries.isEmpty()) {
            logger.info("Add " + newEntries.size() + "/" + entries.size() + " entries from: " + siteLink);
        }

        return newEntries;
    }

    /**
     * convert feed to a list of entry which it's entry content is not initialized
     *
     * @param feed data fetched from site with roman library
     * @return entry
     */
    public List<Entry> getEntries(SyndFeed feed) {
        List<Entry> newEntries = new ArrayList<>();
        for (SyndEntry syndEntry : feed.getEntries()) {
            Entry entry = new Entry();
            entry.setTitle(syndEntry.getTitle());
            entry.setChannel(feed.getTitle());
            if (syndEntry.getDescription() != null)
                entry.setDescription(new Description(
                        syndEntry.getDescription().getType(),
                        syndEntry.getDescription().getMode(),
                        syndEntry.getDescription().getValue()));
            entry.setLink(syndEntry.getLink());
            entry.setPublicationDate(LocalDateTime.ofInstant(syndEntry.getPublishedDate().toInstant(), ZoneId.systemDefault()));
            newEntries.add(entry);
        }
        return newEntries;
    }

    /**
     * get article content of a site from a link
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
            Readability4J readability4J = new Readability4J(link, html);
            Article article = readability4J.parse();
            return article.getTextContent();
        } catch (MalformedURLException e) {
            throw new ContentExtractingException("Invalid RSS URL: URL=" + link, e);
        } catch (HttpStatusException e) {
            throw new ContentExtractingException("HTTP error fetching URL: Status=" + e.getStatusCode() + " URL=" + e.getUrl(), e);
        } catch (SocketTimeoutException e) {
            throw new ContentExtractingException("HTTP error timeout: URL=" + link, e);
        } catch (IOException e) {
            throw new ContentExtractingException("Unable to extract content: URL=" + link, e);
        }
    }

    /**
     * Fetch an SyndFeed from RSS URL
     *
     * @param url url which is an RSS
     * @return SyndFeed which contain RSS contents
     * @throws SyndFeedException if RSS url link is not valid or unable to fetch data from url
     */
    public SyndFeed fetchFromURL(String url) {
        try {
            URL encodedURL = Utility.encodeURL(url);
            String htmlContent = Jsoup.connect(encodedURL.toString()).get().html();
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new StringReader(htmlContent));
            logger.info("RSS data fetched successfully from: " + url);
            return feed;
        } catch (MalformedURLException e) {
            throw new SyndFeedException("Illegal URL format: " + url, e);
        } catch (FeedException | IOException e) {
            throw new SyndFeedException("Invalid RSS URL: " + url, e);
        }
    }

    /**
     * count of news for each day for each site
     *
     * @param title string which must appeared in the title (optional)
     * @return sorted list of HourReport by year and month and day
     * (average report for each site is DAY_COUNT)
     */
    public List<DateReport> getReports(String title) {
        return entryDAO.getDateReports(title, DAY_COUNT * siteDAO.getCount());
    }

    /**
     * count of news for each hour for each site
     *
     * @param title string which must appeared in the title (optional)
     * @return list of HourReport
     */
    public List<HourReport> getHourReports(String title) {
        return entryDAO.getHourReports(title);
    }
}
