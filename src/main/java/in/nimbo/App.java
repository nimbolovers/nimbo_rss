package in.nimbo;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.io.FeedException;
import in.nimbo.dao.FeedDAO;
import in.nimbo.dao.FeedDAOImpl;
import in.nimbo.service.FeedService;

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
        Scanner scanner = new Scanner(System.in);
        FeedDAO dao = new FeedDAOImpl();
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
                    System.out.println("saved successfully");
                    break;
                case "getAll":
                    List<SyndEntry> feeds = service.getFeeds();
                    show(feeds);
                    break;
                case "search":
                    List<SyndEntry> search = service.getFeeds(strings[1]);
                    show(search);
                    break;
            }
        }
    }

    private void show(List<SyndEntry> entries){
        for (SyndEntry entry:entries) {
            System.out.println(entry.getTitle());
        }
    }
}
