package in.nimbo.application;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Utility {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

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
    public static LocalDateTime getDate(String date) {
        try {
            return LocalDateTime.parse(date, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Unable to convert " + date + " to Date");
        }
    }

    /**
     * remove quotation which surrounding value if presents
     *
     * @param value value
     * @return value without quotation
     */
    public static String removeQuotation(String value) {
        if (value != null && value.length() >= 2 &&
                value.charAt(0) == '\"' &&
                value.charAt(value.length() - 1) == '\"')
            return value.substring(1, value.length() - 1);
        return value;
    }
}
