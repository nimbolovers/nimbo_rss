package in.nimbo.dao;

import in.nimbo.entity.Entry;
import in.nimbo.entity.SiteHourReport;
import in.nimbo.entity.SiteReport;

import java.util.Date;
import java.util.List;

public interface EntryDAO {

    List<Entry> filterEntry(String channel, String contentValue, String titleValue, Date startDate, Date finishDate);

    List<Entry> getEntries();

    Entry save(Entry entry);
    
    boolean contain(Entry entry);

    List<SiteReport> getSiteReports(String title, int limit);

    List<SiteHourReport> getHourReports(String title);
}
