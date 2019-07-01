package in.nimbo.dao;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class FeedDAOTest
{
    private ContentDAO contentDAO;
    @Test
    public void testConnection()
    {
        contentDAO = mock(ContentDAO.class);
        FeedDAO feedDAO = new FeedDAOImpl(contentDAO);
    }
}
