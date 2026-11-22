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

    /**
     * Called when a player kicks the ball forward.
     *
     * @param kicker Side of the player who kicked the ball
     */
    default void onBallKicked(Side kicker){}

    /**
     * Called when a player activates their special power aura.
     *
     * @param side Side of the player activating power
     */
    default void onPowerActivated(Side side){}

    /**
     * Called when a player initiates a special power shot on the ball.
     *
     * @param shooter Side of the shooting player
     * @param country Country of the shooting player
     */
    default void onPowerShot(Side shooter, Country country){}

    /**
     * Called when an active power shot hits the opponent player.
     *
     * @param victim Side of the opponent player hit
     * @param damage Health damage dealt by the power shot
     */
    default void onPowerHit(Side victim, double damage){}

    /**
     * Called when persisting player profiles at match end fails.
     *
     * @param message Error message describing the failure
     */
    default void onProfileSaveFailed(String message){}
}