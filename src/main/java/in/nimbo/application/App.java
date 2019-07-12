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
    private Schedule schedule;
    private RSSService rssService;

    public static void main(String[] args) {
        Utility.disableJOOQLogo();

        App app = new App();
        app.init();
        app.doSchedule();
        app.run();
    }

    public App() {
    }

    public App(Schedule schedule, RSSService rssService) {
        this.schedule = schedule;
        this.rssService = rssService;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public RSSService getRssService() {
        return rssService;
    }

    public void init() {
        DescriptionDAO descriptionDAO = new DescriptionDAOImpl();
        ContentDAO contentDAO = new ContentDAOImpl();
        EntryDAO entryDAO = new EntryDAOImpl(descriptionDAO, contentDAO);
        SiteDAO siteDAO = new SiteDAOImpl();
        rssService = new RSSService(entryDAO, siteDAO, contentDAO);
        schedule = new Schedule(rssService);
    }

    public void doSchedule() {
        List<Site> sites = rssService.getSiteDAO().getSites();
        for (Site site : sites) {
            schedule.scheduleSite(site);
        }
        schedule.scheduleSiteDAO(sites);
    }

    private void run() {
        Utility.printlnCLI("Welcome to the RSS service.");
        Utility.printlnCLI();
        Utility.printlnCLI("Type '--help' for help.");

        // UI interface
        Scanner in = new Scanner(System.in);
        Utility.printCLI("rss> ");
        while (in.hasNextLine()) {
            String input = in.nextLine().trim();
            List<String> args = splitArguments(input);
            CommandLine.call(new RssCLI(this), args.toArray(new String[0]));
            Utility.printCLI("rss> ");
        }
    }

    public List<String> splitArguments(String input) {
        List<String> args = new ArrayList<>();
        Matcher m = Pattern.compile("(([^\\s\"]+|\".*?\")+)").matcher(input);
        while (m.find())
            args.add(m.group(1));
        return args;
    }
}
