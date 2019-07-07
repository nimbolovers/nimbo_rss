package in.nimbo.application;

import in.nimbo.application.cli.RssCLI;
import in.nimbo.dao.*;
import in.nimbo.entity.Site;
import in.nimbo.service.RSSService;
import in.nimbo.service.schedule.Schedule;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {
    SiteDAO siteDAO;
    public Schedule schedule;
    RSSService rssService;

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

    public SiteDAO getSiteDAO() {
        return siteDAO;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public RSSService getRssService() {
        return rssService;
    }

    private void run() {
        System.out.println("Welcome to the RSS service.");
        System.out.println();
        System.out.println("Type '--help' for help.");

        // UI interface
        Scanner in = new Scanner(System.in);
        System.out.print("rss> ");
        while (in.hasNextLine()) {
            String input = in.nextLine().trim();
            List<String> args = new ArrayList<>();
            Matcher m = Pattern.compile("(([^\\s\"]+|\".*?\")+)").matcher(input);
            while (m.find())
                args.add(m.group(1));
            CommandLine.call(new RssCLI(this), args.toArray(new String[0]));
            System.out.print("rss> ");
        }
    }
}
