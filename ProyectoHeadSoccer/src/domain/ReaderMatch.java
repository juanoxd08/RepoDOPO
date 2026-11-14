package domain;

/**
 * Read-only interface exposing match data to external controllers and observers.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public interface ReaderMatch {

    Player getPlayer1();

    Player getPlayer2();

    Field getField();

    Ball getBall();

    ScoreBoard getScoreBoard();

    GameStatus getState();

    MatchTimer getTimer();

    MatchType getMatchType();

    int getTargetGoals();
}
