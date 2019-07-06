package in.nimbo.application;

import in.nimbo.dao.*;
import in.nimbo.entity.Entry;
import in.nimbo.entity.Site;
import in.nimbo.service.RSSService;
import in.nimbo.service.Utility;
import in.nimbo.service.schedule.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class App {
    private static Logger logger = LoggerFactory.getLogger(App.class);
    private static SiteDAO siteDAO = new SiteDAOImpl();
    private static List<Site> sites;
    private static Schedule schedule;
    private static RSSService service;

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static void main(String[] args) {
        // Initialization
        // Dependency Injection
        DescriptionDAO descriptionDAO = new DescriptionDAOImpl();
        ContentDAO contentDAO = new ContentDAOImpl();
        EntryDAO entryDAO = new EntryDAOImpl(descriptionDAO, contentDAO);
        service = new RSSService(entryDAO);

        Utility.disableJOOQLogo();

        // Initialize Schedule Service
        schedule = new Schedule(service, sites);

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
                    if (Site.containLink(sites, link)) {
                        logger.warn("Duplicate URL: " + link);
                        continue;
                    }
                    Site newSite = new Site(name, link);
                    sites.add(newSite);
                    schedule.scheduleSite(newSite);
                    break;
                case "search":
                    String paramLine = input.nextLine().trim();
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
                        System.out.println("Illegal parameter format");
                        continue;
                    }
                    if (params.containsKey("title") && params.containsKey("content")) {
                        System.out.println("Illegal parameter: assign to both title and content in not possible");
                        continue;
                    } else if (!params.containsKey("title") && !params.containsKey("content")) {
                        System.out.println("title or content tag must be provided");
                        continue;
                    }
                    String channel = null;
                    Date startDate = null, finishDate = null;
                    if (params.containsKey("channel"))
                        channel = params.get("channel");
                    if (params.containsKey("startDate")) {
                        try {
                            LocalDateTime startLocalDate = LocalDateTime.parse(params.get("startDate"), formatter);
                            startDate = Date.from(startLocalDate.atZone(ZoneId.systemDefault()).toInstant());
                        } catch (DateTimeParseException e) {
                            System.out.println("Illegal start date: " + params.get("startDate"));
                            continue;
                        }
                    }
                    if (params.containsKey("finishDate")) {
                        try {
                            LocalDateTime startLocalDate = LocalDateTime.parse(params.get("finishDate"), formatter);
                            finishDate = Date.from(startLocalDate.atZone(ZoneId.systemDefault()).toInstant());
                        } catch (DateTimeParseException e) {
                            System.out.println("Illegal finish date: " + params.get("finishDate"));
                            continue;
                        }
                    }
                    List<Entry> resultEntry;
                    if (params.containsKey("content"))
                        resultEntry = service.filterEntryByContent(channel, params.get("content"), startDate, finishDate);
                    else
                        resultEntry = service.filterEntryByTitle(channel, params.get("title"), startDate, finishDate);
                    show(resultEntry);
                    break;
                case "exit":
                    break Outer;
            }
        }
    }

    /**
     * show a list of entries in tabular format
     *
     * @param entries entries to print
     */
    private static void show(List<Entry> entries) {
        for (Entry entry : entries) {
            System.out.println("Channel: " + entry.getChannel());
            System.out.println("Title: " + entry.getTitle());
        }
    }
}
