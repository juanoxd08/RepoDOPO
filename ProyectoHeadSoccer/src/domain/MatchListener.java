package domain;

/**
 * Observer interface for match lifecycle events emitted by the domain.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public interface MatchListener{
    /**
     * Called when a player scores a goal.
     *
     * @param scorer Side of the scoring player
     */
    default void onGoal(Side scorer){}

    /**
     * Called when a player is stunned due to zero health.
     *
     * @param victim Side of the stunned player
     */
    default void onStunned(Side victim){}

    /**
     * Called when a match finishes and rewards have been distributed.
     *
     * @param winner Winning side, or null if tied
     * @param winnerPoints Total points of the winning player
     * @param loserPoints Total points of the losing player
     */
    default void onMatchFinished(Side winner, int winnerPoints, int loserPoints){}
}