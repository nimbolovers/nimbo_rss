package in.nimbo.entity;

import com.rometools.rome.feed.synd.SyndEntry;

/**
 * Wrapper for class SyndEntry which contain more information related to entry
 */
public class Entry {
    private int id;
    private String channel;
    private SyndEntry syndEntry;

    public Entry() {
    }

    public Entry(String channel, SyndEntry syndEntry) {
        this.channel = channel;
        this.syndEntry = syndEntry;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setSyndEntry(SyndEntry syndEntry) {
        this.syndEntry = syndEntry;
    }

    public int getId() {
        return id;
    }

    public String getChannel() {
        return channel;
    }

    public SyndEntry getSyndEntry() {
        return syndEntry;
    }
}
