package in.nimbo.entity;

import java.util.Objects;

public class Description {
    private int id;
    private String type;
    private String mode;
    private String value;
    private int feed_id;

    public Description() {
    }

    public Description(String type, String mode, String value) {
        this.type = type;
        this.mode = mode;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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

}
