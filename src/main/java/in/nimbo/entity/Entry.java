package in.nimbo.entity;

import java.util.Date;
import java.util.Objects;


public class Entry {
    private int id;
    private String channel;
    private String title;
    private String link;
    private Date publicationDate;
    private Description description;
    private String content;

    public Entry() {
        // field set by setter/getter
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public int getId() {
        return id;
    }

    public String getChannel() {
        return channel;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    /**
     * check equality of two entries
     * description doesn't check for equality
     * @param o other entry
     * @return true if this.equals(o)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Entry entry = (Entry) o;

        return Objects.equals(channel, entry.channel) &&
                Objects.equals(id, entry.id) &&
                Objects.equals(content, entry.content) &&
                Objects.equals(title, entry.title) &&
                Objects.equals(link, entry.link);
    }
}
