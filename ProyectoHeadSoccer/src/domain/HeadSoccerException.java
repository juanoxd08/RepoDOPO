package domain;

public class HeadSoccerException extends Exception {
    public static final String PLAYER_STATS_OUT_OF_BOUNDS = "Player stats must be between 1 and 5.";

    public HeadSoccerException(String message) {
        super(message);
    }
}
