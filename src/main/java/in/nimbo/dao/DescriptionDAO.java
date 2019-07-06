package in.nimbo.dao;

import in.nimbo.entity.Description;

public interface DescriptionDAO {
    Description getByFeedId(int feedId);

    Description save(Description description);
}
