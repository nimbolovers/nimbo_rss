package in.nimbo.entity;

import com.rometools.rome.feed.synd.SyndContent;

/**
 * Wrapper for class SyndContent which contain more information related to content
 */
public class Content {
    private int id;
    private String relation;
    private SyndContent syndContent;
    private int feed_id;

    public Content() {
    }

    public Content(String relation, SyndContent syndContent) {
        this.relation = relation;
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

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public SyndContent getSyndContent() {
        return syndContent;
    }

    public void setSyndContent(SyndContent syndContent) {
        this.syndContent = syndContent;
    }
}
