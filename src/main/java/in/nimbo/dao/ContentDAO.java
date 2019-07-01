package in.nimbo.dao;

import com.rometools.rome.feed.synd.SyndContent;
import in.nimbo.entity.Content;

import java.util.List;

public interface ContentDAO {
    Content get(int id);

    List<Content> getByFeedId(int feedId);

    Content save(Content content);
}
