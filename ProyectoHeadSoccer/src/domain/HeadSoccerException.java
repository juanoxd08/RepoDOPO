package domain;

/**
 * Custom domain exception for invalid operations and constraints in POOBSoccer.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class HeadSoccerException extends Exception {
    public static final String PLAYER_STATS_OUT_OF_BOUNDS = "Player stats must be between 1 and 5.";

    /**
     * Constructs a HeadSoccerException with the specified error message.
     * 
     * @param message Explanatory message for the exception
     */
    public HeadSoccerException(String message) {
        super(message);
    }
}
