package in.nimbo.dao;

import in.nimbo.entity.Site;

import java.util.List;

public interface SiteDAO {
    Site save(Site site);

    List<Site> getSites();

    boolean containSite(String url);
}
