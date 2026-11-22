package domain;

/**
 * Nigeria signature power shot: Tornado.
 * Launches a rapid ground-bouncing vortex shot that inflicts the Knocked Out
 * status effect for 3.0 seconds and deals 40 base power damage.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class TornadoPower extends Power {

    /**
     * Constructs Nigeria's Tornado power.
     */
    public TornadoPower() {
        super("Tornado", 3.0);
    }

    /**
     * Launches the ball forward with strong downward bounce velocity.
     *
     * @param ball Ball in play
     * @param side Side of the shooting player
     */
    @Override
    protected void shapeShot(Ball ball, Side side) {
        if (ball == null) return;
        double dir = (side == Side.LEFT) ? 1.0 : -1.0;
        ball.setHorizontalSpeed(17.0 * dir);
        ball.setVerticalSpeed(14.0);
    }

    /**
     * Returns the base power damage for Tornado.
     *
     * @return 40.0
     */
    @Override
    public double getBaseDamage() {
        return 40.0;
    }

    /**
     * Creates the KNOCKED_OUT effect applied on impact.
     *
     * @return KnockedOutEffect instance
     */
    @Override
    public Effect createEffect() {
        return new KnockedOutEffect();
    }
}
