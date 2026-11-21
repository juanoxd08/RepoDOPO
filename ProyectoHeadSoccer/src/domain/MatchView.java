package domain;

import java.util.List;
import java.util.Optional;

/**
 * Read-only view of the match state for presentation and AI controllers.
 * 
 * @author Juan Espitia
 * @author Cristian Salamanca
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
    Modality getModality();
    List<Circle> getFakeBalls();
}
