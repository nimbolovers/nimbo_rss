package in.nimbo.entity;

import com.rometools.rome.feed.synd.SyndContent;

/**
 * Wrapper for description (SyndContent) which contain more information related to description
 */
public class Description {
    private int id;
    private SyndContent syndContent;
    private int feed_id;

    public Description() {
    }

    public Description(SyndContent syndContent) {
        this.syndContent = syndContent;
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

    public SyndContent getSyndContent() {
        return syndContent;
    }

    public void setSyndContent(SyndContent syndContent) {
        this.syndContent = syndContent;
    }
}
