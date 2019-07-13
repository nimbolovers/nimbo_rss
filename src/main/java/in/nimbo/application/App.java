package in.nimbo.application;

import in.nimbo.application.cli.RssCLI;
import in.nimbo.dao.*;
import in.nimbo.dao.pool.ConnectionPool;
import in.nimbo.entity.Site;
import in.nimbo.exception.ConnectionException;
import in.nimbo.service.RSSService;
import in.nimbo.service.schedule.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {
    private Logger logger = LoggerFactory.getLogger(App.class);
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
        try {
            ConnectionPool connectionPool = new ConnectionPool();
            DescriptionDAO descriptionDAO = new DescriptionDAOImpl(connectionPool);
            ContentDAO contentDAO = new ContentDAOImpl(connectionPool);
            EntryDAO entryDAO = new EntryDAOImpl(connectionPool, descriptionDAO, contentDAO);
            SiteDAO siteDAO = new SiteDAOImpl(connectionPool);
            rssService = new RSSService(entryDAO, siteDAO, contentDAO);
            schedule = new Schedule(rssService);
        } catch (ConnectionException e) {
            logger.error(e.getMessage(), e);
            Utility.printlnCLI(e.getMessage());
            Utility.printlnCLI("Please check your database initialization and try again!");
        }
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
