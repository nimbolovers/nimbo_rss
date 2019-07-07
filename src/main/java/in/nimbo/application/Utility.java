package in.nimbo.application;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class Utility {
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

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
    public static URL encodeURL(String urlLink) {
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
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Illegal URI syntax", e);
        }
    }

    /**
     * convert a string of date to java.util.Date
     * @param date string format of day
     * @return java.util.date represent given date
     */
    public static Date getDate(String date) {
        try {
            LocalDateTime startLocalDate = LocalDateTime.parse(date, formatter);
            return Date.from(startLocalDate.atZone(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Unable to convert " + date + " to Date");
        }
    }
}
