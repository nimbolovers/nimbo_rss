package in.nimbo.application;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utility {
    public static SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    private Utility() {}

    /**
     * disable JOOQ library logger
     */
    public static void disableJOOQLogo() {
        System.getProperties().setProperty("org.jooq.no-logo", "true");
    }

    /**
     * encode a url which maybe contain UTF-8 characters
     * @param urlLink link
     * @return encoded URL
     */
    public static URL encodeURL(String urlLink) throws MalformedURLException {
        try {
            if (urlLink.contains("%")) // it is encoded, so just return
                return new URL(urlLink);
            else
            {
                // encode url link
                URL url = new URL(urlLink);
                URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
                return new URL(uri.toASCIIString());
            }
        } catch (MalformedURLException | URISyntaxException e) {
            throw new MalformedURLException("Illegal URL: " + urlLink);
        }
    }

    /**
     * convert a string of date to java.util.Date
     * @param date string format of day
     * @return java.util.date represent given date
     * @throws IllegalArgumentException if unable to convert string to date
     */
    public static Date getDate(String date) {
        try {
            return formatter.parse(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Unable to convert " + date + " to Date");
        }
    }
}
