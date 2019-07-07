package in.nimbo.application;

import in.nimbo.dao.*;
import in.nimbo.entity.Entry;
import in.nimbo.entity.Site;
import in.nimbo.exception.RssServiceException;
import in.nimbo.service.RSSService;
import in.nimbo.service.schedule.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class App {
    private static Logger logger = LoggerFactory.getLogger(App.class);
    private SiteDAO siteDAO;
    private Schedule schedule;
    private RSSService rssService;

    public static void main(String[] args) {
        Utility.disableJOOQLogo();

        // Initialization
        // Dependency Injection
        DescriptionDAO descriptionDAO = new DescriptionDAOImpl();
        ContentDAO contentDAO = new ContentDAOImpl();
        EntryDAO entryDAO = new EntryDAOImpl(descriptionDAO, contentDAO);
        RSSService rssService = new RSSService(entryDAO);

        // Initialize Schedule Service
        List<Site> sites = new ArrayList<>();
        Schedule schedule = new Schedule(rssService, sites);

        // Load sites
        SiteDAO siteDAO = new SiteDAOImpl();
        sites = siteDAO.getSites();
        for (Site site : sites) {
            schedule.scheduleSite(site);
        }

        App app = new App(siteDAO, schedule, rssService);
        app.run();
    }

    public App(SiteDAO siteDAO, Schedule schedule, RSSService rssService) {
        this.siteDAO = siteDAO;
        this.schedule = schedule;
        this.rssService = rssService;
    }

    private void run() {
        logger.info("Application started successfully");

        // UI interface
        runUI();

        schedule.stopService();

        // Save sites before exit
        for (Site site : schedule.getSites()) {
            if (site.getId() != 0)
                siteDAO.update(site);
            else
                siteDAO.save(site);
        }
    }

    /**
     * User interface of application
     */
    private void runUI() {
        Scanner input = new Scanner(System.in);
        Outer:
        while (input.hasNextLine()) {
            String command = input.next();
            switch (command) {
                case "add":
                    String name = input.next();
                    String link = input.next();
                    try {
                        addSite(name, link);
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
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

                    try {
                        List<Entry> resultEntry;
                        if (isTitleSearch)
                            resultEntry = rssService.filterEntryByTitle(channel, searchString, startDate, finishDate);
                        else
                            resultEntry = rssService.filterEntryByContent(channel, searchString, startDate, finishDate);
                        showEntries(resultEntry);
                    } catch (RssServiceException e) {
                        System.out.println("Unable to search for data");
                    }
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
     * @throws IllegalArgumentException if site is duplicate
     */
    public void addSite(String name, String link) {
        if (Site.containLink(schedule.getSites(), link))
            throw new IllegalArgumentException("Duplicate URL: " + link);

        Site newSite = new Site(name, link);
        schedule.getSites().add(newSite);
        schedule.scheduleSite(newSite);
    }

    /**
     * show a list of entries in tabular format
     *
     * @param entries entries to print
     */
    public static void showEntries(List<Entry> entries) {
        for (Entry entry : entries) {
            System.out.println("Channel: " + entry.getChannel());
            System.out.println("Title: " + entry.getTitle());
        }
    }
}
