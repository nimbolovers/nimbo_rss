package in.nimbo.service.schedule;

import in.nimbo.entity.Site;
import in.nimbo.exception.RssServiceException;
import in.nimbo.service.RSSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * update site in database frequently
 */
public class ScheduleSiteUpdater implements Runnable {
    private Logger logger = LoggerFactory.getLogger(ScheduleSiteUpdater.class);
    private List<Site> sites;
    private RSSService rssService;

    public ScheduleSiteUpdater(List<Site> sites, RSSService rssService) {
        this.sites = sites;
        this.rssService = rssService;
    }

    @Override
    public void run() {
        // Save sites before exit
        for (Site site : sites) {
            try {
                rssService.updateSite(site);
            } catch (RssServiceException e) {
                logger.error("Unable to save information of site on database: " + site.getName());
            }
        }
    }
}
