package in.nimbo.entity;

import java.util.List;

public class Site {
    private int id;
    private String name;
    private String link;

    public Site() {
    }

    public Site(String name, String link) {
        this.name = name;
        this.link = link;
    }

    public static boolean containLink(List<Site> sites, String link) {
        return sites.stream().map(Site::getLink).anyMatch(l -> l.equals(link));
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
}
