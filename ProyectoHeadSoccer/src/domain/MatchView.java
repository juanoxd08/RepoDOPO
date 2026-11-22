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

    /**
     * Returns the current lifecycle status of the match.
     *
     * @return Active GameStatus
     */
    GameStatus getStatus();

    /**
     * Returns the remaining match time in seconds, or -1 for goal-based matches.
     *
     * @return Remaining seconds or -1
     */
    int getRemainingSeconds();

    /**
     * Indicates whether the match is currently in sudden-death golden goal mode.
     *
     * @return True if sudden death is active
     */
    boolean isSuddenDeath();

    /**
     * Returns the number of goals scored by the specified side.
     *
     * @param side Target side
     * @return Goal count for the side
     */
    int getGoals(Side side);

    /**
     * Returns a read-only view of the player on the specified side.
     *
     * @param side Target side
     * @return PlayerView for the side
     */
    PlayerView getPlayer(Side side);

    /**
     * Returns a read-only view of the ball in play.
     *
     * @return BallView instance
     */
    BallView getBall();

    /**
     * Returns the winning side if the match has concluded with a winner.
     *
     * @return Optional containing the winning side, or empty if undecided or tied
     */
    Optional<Side> getWinner();

    /**
     * Returns the logical width of the playing field.
     *
     * @return Field width in logical units
     */
    int getFieldWidth();

    /**
     * Returns the logical height of the playing field.
     *
     * @return Field height in logical units
     */
    int getFieldHeight();

    /**
     * Returns a defensive copy of the goal scoring area for the given side.
     *
     * @param side Goal side
     * @return Goal area rectangle
     */
    Rectangle getGoalArea(Side side);

    /**
     * Returns a defensive copy of the goal crossbar hitbox for the given side.
     *
     * @param side Goal side
     * @return Crossbar rectangle
     */
    Rectangle getCrossBar(Side side);

    /**
     * Returns the game modality configured for this match.
     *
     * @return Active Modality
     */
    Modality getModality();

    /**
     * Returns defensive copies of any active illusion decoy balls on the field.
     *
     * @return List of decoy ball circles
     */
    List<Circle> getFakeBalls();
}
