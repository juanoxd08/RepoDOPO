package domain;

import java.util.Optional;

/**
 * Read-only view of the match state for presentation and AI controllers.
 */
public interface MatchView {
    GameStatus getStatus();
    int getRemainingSeconds();
    boolean isSuddenDeath();
    int getGoals(Side side);
    PlayerView getPlayer(Side side);
    BallView getBall();
    Optional<Side> getWinner();
    int getFieldWidth();
    int getFieldHeight();
    Rectangle getGoalArea(Side side);
    Rectangle getCrossBar(Side side);
}
