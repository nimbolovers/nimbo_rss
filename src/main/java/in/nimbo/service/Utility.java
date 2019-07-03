package in.nimbo.service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class Utility {

    private Utility() {}

    /**
     * encode a url which maybe contain UTF-8 characters
     * @param urlLink link
     * @return encoded URL link
     */
    public static String encodeURL(String urlLink) {
        try {
            URL url = new URL(urlLink);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            return uri.toASCIIString();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Illegal URI syntax", e);
        }
    }
}
