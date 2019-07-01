package in.nimbo.entity;

import com.rometools.rome.feed.synd.SyndEntry;

/**
 * Wrapper for class SyndEntry which contain more information related to entry
 */
public class Entry {
    private String channel;
    private SyndEntry syndEntry;

    public Entry(String channel, SyndEntry syndEntry) {
        this.channel = channel;
        this.syndEntry = syndEntry;
    }
}
