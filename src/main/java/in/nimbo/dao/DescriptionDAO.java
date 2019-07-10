package in.nimbo.dao;

import in.nimbo.entity.Description;

import java.util.Optional;

public interface DescriptionDAO {
    Optional<Description> getByFeedId(int feedId);

    Description save(Description description);
}
