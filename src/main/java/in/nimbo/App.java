package in.nimbo;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.io.FeedException;
import in.nimbo.dao.ContentDAO;
import in.nimbo.dao.ContentDAOImpl;
import in.nimbo.dao.FeedDAO;
import in.nimbo.dao.FeedDAOImpl;
import in.nimbo.entity.Entry;
import in.nimbo.service.FeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
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
    public App(FeedService service, Scanner scanner) throws IOException {
        this.service = service;
        this.scanner = scanner;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        properties = new Properties();
        InputStream is = loader.getResourceAsStream("sites.properties");
        properties.load(is);

    }
    public static void main( String[] args ) throws IOException, FeedException {
        Logger logger = LoggerFactory.getLogger(App.class);
        logger.info("test");
        Scanner scanner = new Scanner(System.in);
        ContentDAO contentDAO = new ContentDAOImpl();
        FeedDAO dao = new FeedDAOImpl(contentDAO);
        FeedService service = new FeedService(dao);
        App app = new App(service, scanner);
        app.run();
    }
    public void run() throws IOException, FeedException {
        while (scanner.hasNextLine()){
            String line = scanner.nextLine();
            String[] strings = line.split(" ");
            switch (strings[0]){
                case "save":
                    service.save(properties.getProperty(strings[1]));
                    break;
                case "getAll":
                    List<Entry> feeds = service.getFeeds();
                    show(feeds);
                    break;
                case "search":
                    List<Entry> search = service.getFeeds(strings[1]);
                    show(search);
                    break;
            }
        }
    }

    private void show(List<Entry> entries){
        for (Entry entry:entries) {
            System.out.println(entry.getChannel() + " "  + entry.getSyndEntry().getTitle());
        }
    }
}
