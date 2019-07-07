package in.nimbo;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class AppTest {

    @BeforeClass
    public static void init() {
        TestUtility.disableJOOQLogo();
    }
}
