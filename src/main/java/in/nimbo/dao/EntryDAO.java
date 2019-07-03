package in.nimbo.dao;

import in.nimbo.entity.Entry;

import java.util.Date;
import java.util.List;

public interface EntryDAO {

    List<Entry> filterEntryByTitle(String channel, String value, Date startDate, Date finishDate);

    List<Entry> filterEntryByContent(String channel, String value, Date startDate, Date finishDate);

    List<Entry> getEntries();

    Entry save(Entry entry);

    /**
     * check whether an entry exists in database
     *
     * @param entry which is checked
     * @return true if entry exists in database
     * based on entry.title and entry.channel
     */
    boolean contain(Entry entry);
}
