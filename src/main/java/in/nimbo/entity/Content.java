package in.nimbo.entity;

/**
 * contain content of RSS which collected from link of RSS feed
 */
public class Content {
    private int id;
    private String value;
    private int feed_id;

    public Content() {
    }

    public Content(String value, int feed_id) {
        this.value = value;
        this.feed_id = feed_id;
    }

    public int getFeed_id() {
        return feed_id;
    }

    public void setFeed_id(int feed_id) {
        this.feed_id = feed_id;
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
