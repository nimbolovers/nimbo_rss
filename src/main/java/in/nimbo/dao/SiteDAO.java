package in.nimbo.dao;

import in.nimbo.entity.Site;

import java.util.List;

public interface SiteDAO {
    boolean containLink(String link);

    Site save(Site site);

    Site update(Site site);

    List<Site> getSites();

    int getCount();
}
