package in.nimbo.dao;

import in.nimbo.entity.Content;

import java.util.Optional;

public interface ContentDAO {

    Optional<Content> getByFeedId(int feedId);

    Content save(Content content);
}
