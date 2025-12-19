package reproducer.logger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AdminLogger {

    private static final Logger ADMIN_LOG = LogManager.getLogger("adminLog");

    public static void trace(String msg) {
        ADMIN_LOG.trace(msg);
    }

}
