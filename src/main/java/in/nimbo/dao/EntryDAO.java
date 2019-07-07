package in.nimbo.dao;

import in.nimbo.entity.Entry;
import in.nimbo.entity.SiteReport;

import java.util.Date;
import java.util.List;

public interface EntryDAO {

    List<Entry> filterEntryByTitle(String channel, String value, Date startDate, Date finishDate);

    List<Entry> filterEntryByContent(String channel, String value, Date startDate, Date finishDate);

    List<Entry> getEntries();

    Entry save(Entry entry);
    
    boolean contain(Entry entry);

    List<SiteReport> getSiteReports(String title, int limit);
}
