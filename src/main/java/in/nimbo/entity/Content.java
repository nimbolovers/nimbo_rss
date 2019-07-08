package in.nimbo.entity;

/**
 * contain content of RSS which collected from link of RSS feed
 */
public class Content {
    private int id;
    private String value;
    private int feedId;

    public Content() {
    }

    public Content(String value, int feedId) {
        this.value = value;
        this.feedId = feedId;
    }

    public int getFeedId() {
        return feedId;
    }

    public void setFeedId(int feedId) {
        this.feedId = feedId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
