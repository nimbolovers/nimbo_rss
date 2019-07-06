package in.nimbo.dao;

import in.nimbo.entity.Content;

public interface ContentDAO {

    Content getByFeedId(int feedId);

    Content save(Content content);
}
