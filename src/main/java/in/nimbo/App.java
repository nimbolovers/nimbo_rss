package in.nimbo;

import com.rometools.rome.io.FeedException;
import in.nimbo.dao.DescriptionDAO;
import in.nimbo.dao.DescriptionDAOImpl;
import in.nimbo.dao.FeedDAO;
import in.nimbo.dao.FeedDAOImpl;
import in.nimbo.entity.Entry;
import in.nimbo.service.FeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class App {
    private FeedService service;
    private Scanner scanner;
    private Properties properties;
    private Logger logger = LoggerFactory.getLogger(App.class);
    public App(FeedService service, Scanner scanner) throws IOException {
        this.service = service;
        this.scanner = scanner;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        properties = new Properties();
        InputStream is = loader.getResourceAsStream("sites.properties");
        properties.load(is);

    }
    public static void main( String[] args ) throws IOException, FeedException {
        Scanner scanner = new Scanner(System.in);
        DescriptionDAO descriptionDAO = new DescriptionDAOImpl();
        FeedDAO dao = new FeedDAOImpl(descriptionDAO);
        FeedService service = new FeedService(dao);
        App app = new App(service, scanner);
        app.run();
    }
    public void run() throws IOException, FeedException {
        logger.info("app started successfully");
        while (scanner.hasNextLine()){
            String line = scanner.nextLine();
            String[] strings = line.split(" ");
            try {
                switch (strings[0]) {
                    case "save":
                        service.save(service.fetchFromURL(properties.getProperty(strings[1])));
                        break;
                    case "getAll":
                        List<Entry> feeds = service.getFeeds();
                        show(feeds);
                        break;
                    case "search":
                        List<Entry> search = service.getFeeds(strings[1]);
                        show(search);
                        break;
                    case "add":
                        properties.put(strings[1], strings[2]);
                        logger.info("the site added to my sites " + strings[1] + " " + strings[2]);
                        break;
                }
            }catch (Exception e){
                logger.error(e.getMessage());
            }
        }
    }

    private void show(List<Entry> entries){
        for (Entry entry:entries) {
            logger.info(entry.getChannel() + "\t" + entry.getSyndEntry().getTitle());
        }
    }
}
