package domain;

/**
 * Read-only interface exposing match data to external controllers and observers.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public interface ReaderMatch {

    /**
     * Returns player 1 of the match.
     * 
     * @return Player 1 instance
     */
    Player getPlayer1();

    /**
     * Returns player 2 of the match.
     * 
     * @return Player 2 instance
     */
    Player getPlayer2();

    /**
     * Returns the match field.
     * 
     * @return Field instance
     */
    Field getField();

    /**
     * Returns the match ball.
     * 
     * @return Ball instance
     */
    Ball getBall();

    /**
     * Returns the match scoreboard.
     * 
     * @return ScoreBoard instance
     */
    ScoreBoard getScoreBoard();

    /**
     * Returns the current game status.
     * 
     * @return GameStatus state
     */
    GameStatus getState();
}
