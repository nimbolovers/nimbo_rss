package in.nimbo;

import in.nimbo.dao.*;
import in.nimbo.entity.Entry;
import in.nimbo.service.RSSService;
import in.nimbo.service.Utility;
import in.nimbo.service.schedule.Schedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class App {
    private static Logger logger = LoggerFactory.getLogger(App.class);
    private static Properties siteProp = new Properties();
    private static Schedule schedule;


    public static void main(String[] args) throws IOException {
        // Initialization
        // Dependency Injection
        DescriptionDAO descriptionDAO = new DescriptionDAOImpl();
        ContentDAO contentDAO = new ContentDAOImpl();
        EntryDAO entryDAO = new EntryDAOImpl(descriptionDAO, contentDAO);
        RSSService service = new RSSService(entryDAO);

        // Load sites
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream is = loader.getResourceAsStream("sites.properties");
        siteProp.load(is);

        Utility.disableJOOQLogo();

        // Initialize Schedule Service
        schedule = new Schedule(service);

        logger.info("Application started successfully");

        // UI interface
        runUI();
    }

    private static void runUI() {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.next();
            switch (command) {
                case "add":
                    String url = scanner.next();
                    if (siteProp.getProperty(url) != null) {
                        logger.warn("URL ");
                    }
                    schedule.scheduleRSSLink(siteProp.getProperty(strings[1]));
                    break;
                case "getAll":
                    List<Entry> feeds = service.filterEntryByTitle();
                    show(feeds);
                    break;
                case "search":
                    List<Entry> search = service.filterEntryByContent(null, strings[1], null, null);
                    show(search);
                    break;
                case "add":
                    siteProp.put(strings[1], strings[2]);
                    logger.info("the site added to my sites " + strings[1] + " " + strings[2]);
                    break;
                case "get":
                    show(service.filterEntryByContent(null, strings[1], null, null));
                    break;
            }
        }
    }

    private void show(List<Entry> entries) {
        for (Entry entry : entries) {
            logger.info(entry.getChannel() + "\t" + entry.getSyndEntry().getTitle());
        }
    }
}
