package domain;

import java.io.Serializable;
import java.util.Optional;

/**
 * Strategy interface defining victory and time/goal progression rules for a match.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public interface MatchRule extends Serializable {
    /**
     * Updates rule state on each simulation tick.
     *
     * @param match Active match
     */
    void onTick(Match match);

    /**
     * Checks whether the match has reached its end condition.
     *
     * @param match Active match
     * @return True if the match is over
     */
    boolean isOver(Match match);

    /**
     * Returns the winning side if the match is over.
     *
     * @param match Active match
     * @return Optional containing the winning side, or empty if not over or tied
     */
    public default Optional<Side> winner(Match match){
        if (!isOver(match)) {
            return Optional.empty();
        }
        return match.getScoreBoard().getLeader();
    }

    /**
     * Returns remaining match time in seconds, or negative if not time-based.
     *
     * @param match Active match
     * @return Remaining seconds, or -1 if not applicable
     */
    int remainingSeconds(Match match);
}