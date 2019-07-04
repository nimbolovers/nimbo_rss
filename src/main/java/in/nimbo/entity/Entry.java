package in.nimbo.entity;

import com.rometools.rome.feed.synd.SyndEntry;

import java.util.Objects;

/**
 * Wrapper for class SyndEntry which contain more information related to entry
 */
public class Entry {
    private int id;
    private String channel;
    private String content;
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


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Entry entry = (Entry) o;

        return Objects.equals(channel, entry.channel) &&
                Objects.equals(id, entry.id) &&
                Objects.equals(content, entry.content) &&
                Objects.equals(syndEntry.getTitle(), entry.syndEntry.getTitle()) &&
                Objects.equals(syndEntry.getLink(), entry.syndEntry.getLink());
    }
}
