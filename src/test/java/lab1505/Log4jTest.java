package lab1505;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.jupiter.api.Test;

public class Log4jTest {
    @Test
    public void hello() {
        PropertyConfigurator.configure("log4j.properties");
        Logger logger = Logger.getLogger("Test Logger");
        logger.info("hello");
        logger.info("world");
    }
}
