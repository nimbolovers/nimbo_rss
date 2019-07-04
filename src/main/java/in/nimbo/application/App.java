package in.nimbo.application;

import in.nimbo.dao.*;
import in.nimbo.entity.Entry;
import in.nimbo.entity.Site;
import in.nimbo.service.RSSService;
import in.nimbo.service.Utility;
import in.nimbo.service.schedule.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class App {
    private static Logger logger = LoggerFactory.getLogger(App.class);
    private static SiteDAO siteDAO = new SiteDAOImpl();
    private static List<Site> sites;
    private static Schedule schedule;
    private static RSSService service;


    public static void main(String[] args) {
        // Initialization
        // Dependency Injection
        DescriptionDAO descriptionDAO = new DescriptionDAOImpl();
        ContentDAO contentDAO = new ContentDAOImpl();
        EntryDAO entryDAO = new EntryDAOImpl(descriptionDAO, contentDAO);
        service = new RSSService(entryDAO);

        Utility.disableJOOQLogo();

        // Initialize Schedule Service
        schedule = new Schedule(service);

        // Load sites
        sites = siteDAO.getSites();
        for (Site site : sites) {
            schedule.scheduleRSSLink(site.getLink());
        }

        logger.info("Application started successfully");

        // UI interface
        runUI();
    }

    private static void runUI() {
        Scanner input = new Scanner(System.in);
        while (input.hasNextLine()) {
            String command = input.next();
            switch (command) {
                case "add":
                    String name = input.next();
                    String link = input.next();
                    if (Site.containLink(sites, link)) {
                        logger.warn("Duplicate URL: " + link);
                        continue;
                    }
                    sites.add(new Site(name, link));
                    schedule.scheduleRSSLink(link);
                    break;
                case "search":
                    String paramLine = input.nextLine().trim();
                    Map<String, String> params = Arrays.stream(paramLine.split(" "))
                            .collect(Collectors.toMap((String x) -> x.split("=")[0], (String y) -> y.split("=")[1]));
                    if (params.containsKey("title") && params.containsKey("content")) {
                        System.out.println("Illegal parameter: assign to both title and content in not possible");
                        continue;
                    } else if (!params.containsKey("title") && !params.containsKey("content")) {
                        System.out.println("title or content tag must be provided");
                        continue;
                    }
                    String channel = null, startDate = null, finishDate = null;
                    if (params.containsKey("channel"))
                        channel = params.get("channel");
                    if (params.containsKey("startDate"))
                        startDate = params.get("startDate");
                    if (params.containsKey("finishDate"))
                        finishDate = params.get("finishDate");
                    List<Entry> resultEntry;
                    if (params.containsKey("content"))
                        resultEntry = service.filterEntryByContent(channel, params.get("content"), null, null);
                    else
                        resultEntry = service.filterEntryByTitle(channel, params.get("title"), null, null);
                    show(resultEntry);
                    break;
            }
        }
    }

    /**
     * show a list of entries in tabular format
     * @param entries entries to print
     */
    private static void show(List<Entry> entries) {
        for (Entry entry : entries) {
            System.out.println("Channel: " + entry.getChannel());
            System.out.println("Title: " + entry.getSyndEntry().getTitle());
        }
    }
}
