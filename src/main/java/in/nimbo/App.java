package in.nimbo;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import in.nimbo.dao.FeedDAO;

import java.io.IOException;
import java.net.URL;

/**
 * Hello world!
 *
 */
public class App 
{
    private FeedDAO feedDAO;
    public static void main( String[] args ) throws IOException, FeedException {
        URL url = new URL("https://90tv.ir/rss/news");
        SyndFeedInput syndFeed = new SyndFeedInput();
        SyndFeed build = syndFeed.build(new XmlReader(url));

        System.out.println( "Hello World!" );
    }
}
