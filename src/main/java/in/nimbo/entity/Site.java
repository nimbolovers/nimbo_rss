package in.nimbo.entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class Site {
    private int id;
    private String name;
    private String link;
    private long newsCount;
    private long avgUpdateTime;
    private LocalDateTime lastUpdate;

    public Site() {
    }

    public Site(String name, String link) {
        this.name = name;
        this.link = link;
    }

    public long getAvgUpdateTime() {
        return avgUpdateTime;
    }

    public void setAvgUpdateTime(long avgUpdateTime) {
        this.avgUpdateTime = avgUpdateTime;
    }

    public long getNewsCount() {
        return newsCount;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void increaseNewsCount(long value) {
        this.newsCount += value;
    }

    public void setNewsCount(long newsCount) {
        this.newsCount = newsCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Site site = (Site) o;
        return newsCount == site.newsCount &&
                avgUpdateTime == site.avgUpdateTime &&
                Objects.equals(name, site.name) &&
                Objects.equals(link, site.link) &&
                Objects.equals(lastUpdate, site.lastUpdate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(newsCount, avgUpdateTime, name, link, lastUpdate);
    }
}
