package persistence;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Utility logger recording technical exceptions to a persistent log file for developers.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class Log {
    private static final Logger logger = Logger.getLogger("DOPOSoccer");
    private static boolean initialized = false;

    /*
     * Private constructor for utility class.
     */
    private Log() {
    }

    /**
     * Records an exception in the DOPOSoccer log file.
     *
     * @param e Exception to record
     */
    public static void record(Exception e) {
        if (e == null) return;
        try {
            if (!initialized) {
                FileHandler fh = new FileHandler("DOPOSoccer.log", true);
                fh.setFormatter(new SimpleFormatter());
                logger.addHandler(fh);
                logger.setUseParentHandlers(false);
                initialized = true;
            }
            logger.log(Level.SEVERE, e.getMessage(), e);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
}