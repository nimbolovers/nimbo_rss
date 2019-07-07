package in.nimbo.dao;

import in.nimbo.entity.Entry;

import java.util.Date;
import java.util.List;

public interface EntryDAO {

    List<Entry> filterEntry(String channel, String contentValue, String titleValue, Date startDate, Date finishDate);

    List<Entry> getEntries();

    Entry save(Entry entry);
    
    boolean contain(Entry entry);
}
