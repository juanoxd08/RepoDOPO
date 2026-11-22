package domain;

/**
 * Read-only view of the ball state for presentation and AI controllers.
 * 
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public interface BallView {

    /**
     * Returns the horizontal center coordinate of the ball.
     *
     * @return Horizontal coordinate in field units
     */
    double getX();

    /**
     * Returns the vertical center coordinate of the ball.
     *
     * @return Vertical coordinate in field units
     */
    double getY();

    /**
     * Returns the collision radius of the ball.
     *
     * @return Radius in field units
     */
    double getRadius();

    /**
     * Indicates whether the ball is currently carrying an active power shot.
     *
     * @return True if a power shot is active on the ball
     */
    boolean hasPowerShot();

    /**
     * Returns the current horizontal velocity of the ball.
     *
     * @return Horizontal velocity in field units per tick
     */
    double getHorizontalSpeed();

    /**
     * Returns the current vertical velocity of the ball.
     *
     * @return Vertical velocity in field units per tick
     */
    double getVerticalSpeed();

    /**
     * Returns the country that launched the active power shot, if any.
     *
     * @return Launching country, or null when no power shot is active
     */
    Country getPowerShotCountry();

    /**
     * Indicates whether a power shot is currently in its suspended windup phase.
     *
     * @return True if the power shot is charging
     */
    boolean isPowerShotCharging();

    /**
     * Returns the normalized progress of the power shot windup phase in [0.0, 1.0].
     *
     * @return Normalized charging progress
     */
    double getPowerShotChargeProgress();
}
