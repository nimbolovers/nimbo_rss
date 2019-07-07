package in.nimbo.dao;

import in.nimbo.entity.Entry;
import in.nimbo.entity.report.HourReport;
import in.nimbo.entity.report.DateReport;

import java.util.Date;
import java.util.List;

public interface EntryDAO {

    List<Entry> filterEntry(String channel, String contentValue, String titleValue, Date startDate, Date finishDate);

    List<Entry> getEntries();

    Entry save(Entry entry);
    
    boolean contain(Entry entry);

    List<DateReport> getDateReports(String title, int limit);

    List<HourReport> getHourReports(String title);
}
