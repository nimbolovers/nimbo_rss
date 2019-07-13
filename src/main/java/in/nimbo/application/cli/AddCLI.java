package in.nimbo.application.cli;

import in.nimbo.application.Utility;
import in.nimbo.entity.Site;
import in.nimbo.exception.SyndFeedException;
import in.nimbo.service.RSSService;
import in.nimbo.service.schedule.Schedule;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "add",
        version = RssCLI.VERSION,
        description = "Add a new site to repository")
public class AddCLI implements Callable<Void> {
    @CommandLine.ParentCommand
    private RssCLI rssCLI;

    @CommandLine.Parameters(paramLabel = "name", index = "0", description = "The name alias for link")
    private String siteName;

    @CommandLine.Parameters(paramLabel = "link", index = "1", description = "Link of site")
    private String siteLink;

    @CommandLine.Option(names = {"--help"}, usageHelp = true,
            description = "Display help")
    boolean usageHelpRequested;

    @Override
    public Void call() {
        try {
            addSite(rssCLI.getApp().getSchedule(), rssCLI.getApp().getRssService(), siteName, siteLink);
            Utility.printlnCLI("Site " + siteName + " (" + siteLink + ") added");
        } catch (IllegalArgumentException | SyndFeedException e) {
            Utility.printlnCLI(e.getMessage());
        }
        return null;
    }

    /**
     * add a site to schedule rssService
     * if it is duplicate, ignore it
     *
     * @param name name of site
     * @param link link of site
     * @throws IllegalArgumentException if site is duplicate
     */
    public void addSite(Schedule schedule, RSSService rssService, String name, String link) {
        if (rssService.getSiteDAO().containLink(link))
            throw new IllegalArgumentException("Duplicate URL: " + link);

        // check whether link is illegal or not
        rssService.fetchFeedFromURL(link);
        Site newSite = new Site(name, link);
        rssService.getSiteDAO().save(newSite);
        schedule.scheduleSite(newSite);
    }
}
