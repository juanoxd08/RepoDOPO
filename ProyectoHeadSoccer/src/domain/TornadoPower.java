package domain;

/**
 * Tiro de poder de Nigeria: Tornado.
 * CÃ³mo sale: Remolino a ras de suelo que rebota rÃ¡pidamente.
 * Efecto: Inconsciente (es lanzado al aire y no puede moverse, saltar ni patear por 3 s).
 * DaÃ±oTiro: 40 (DaÃ±o real 8 * 5 = 40).
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
