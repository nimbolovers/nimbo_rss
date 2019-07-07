package in.nimbo.application;

import in.nimbo.dao.*;
import in.nimbo.entity.Entry;
import in.nimbo.entity.Site;
import in.nimbo.service.RSSService;
import in.nimbo.service.schedule.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class App {
    private static Logger logger = LoggerFactory.getLogger(App.class);
    private static SiteDAO siteDAO = new SiteDAOImpl();
    private static Schedule schedule;
    private static RSSService rssService;

    public static void main(String[] args) {
        // Initialization
        // Dependency Injection
        DescriptionDAO descriptionDAO = new DescriptionDAOImpl();
        ContentDAO contentDAO = new ContentDAOImpl();
        EntryDAO entryDAO = new EntryDAOImpl(descriptionDAO, contentDAO);
        rssService = new RSSService(entryDAO);

        Utility.disableJOOQLogo();

        // Initialize Schedule Service
        List<Site> sites = new ArrayList<>();
        schedule = new Schedule(rssService, sites);

        // Load sites
        sites = siteDAO.getSites();
        for (Site site : sites) {
            schedule.scheduleSite(site);
        }

        logger.info("Application started successfully");

        // UI interface
        runUI();

        schedule.stopService();

        // Save sites before exit
        for (Site site : sites) {
            if (site.getId() != 0)
                siteDAO.update(site);
            else
                siteDAO.save(site);
        }
    }

    /**
     * User interface of application
     */
    private static void runUI() {
        Scanner input = new Scanner(System.in);
        Outer:
        while (input.hasNextLine()) {
            String command = input.next();
            switch (command) {
                case "add":
                    String name = input.next();
                    String link = input.next();
                    addSite(name, link);
                    break;
                case "search":
                    boolean isTitleSearch;
                    String searchString;
                    String channel = null;
                    Date startDate = null, finishDate = null;
                    try {
                        Map<String, String> params;
                        params = getParameters(input.nextLine().trim());
                        if (params.containsKey("channel"))
                            channel = params.get("channel");
                        if (params.containsKey("startDate"))
                            startDate = Utility.getDate(params.get("startDate"));
                        if (params.containsKey("finishDate"))
                            finishDate = Utility.getDate(params.get("finishDate"));
                        if (params.containsKey("content")) {
                            searchString = params.get("content");
                            isTitleSearch = false;
                        } else {
                            searchString = params.get("title");
                            isTitleSearch = true;
                        }
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                        continue;
                    }

                    List<Entry> resultEntry;
                    if (isTitleSearch)
                        resultEntry = rssService.filterEntryByTitle(channel, searchString, startDate, finishDate);
                    else
                        resultEntry = rssService.filterEntryByContent(channel, searchString, startDate, finishDate);
                    showEntries(resultEntry);
                    break;
                case "exit":
                    break Outer;
            }
        }
    }

    /**
     * convert a string of arguments to a map of key/values
     *
     * @param paramLine parameters
     * @return map of parameters
     */
    private static Map<String, String> getParameters(String paramLine) {
        Map<String, String> params;
        try {
            params = Arrays.stream(paramLine.split("-"))
                    .filter(s -> !s.trim().isEmpty())
                    .collect(Collectors.toMap(
                            (String x) -> x.trim().split("=")[0],
                            (String y) -> {
                                String value = y.trim().split("=")[1];
                                return value.substring(1, value.length() - 1);
                            }));
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Illegal parameter format", e);
        }

        if (params.containsKey("title") && params.containsKey("content")) {
            throw new IllegalArgumentException("Illegal parameter: assign to both title and content in not possible");
        } else if (!params.containsKey("title") && !params.containsKey("content")) {
            throw new IllegalArgumentException("title or content tag must be provided");
        }
        return params;
    }

    /**
     * add a site to schedule rssService
     * if it is duplicate, ignore it
     *
     * @param name name of site
     * @param link link of site
     */
    private static void addSite(String name, String link) {
        if (Site.containLink(schedule.getSites(), link)) {
            logger.warn("Duplicate URL: " + link);
            return;
        }
        Site newSite = new Site(name, link);
        schedule.getSites().add(newSite);
        schedule.scheduleSite(newSite);
    }

    /**
     * show a list of entries in tabular format
     *
     * @param entries entries to print
     */
    private static void showEntries(List<Entry> entries) {
        for (Entry entry : entries) {
            System.out.println("Channel: " + entry.getChannel());
            System.out.println("Title: " + entry.getTitle());
        }
    }
}
