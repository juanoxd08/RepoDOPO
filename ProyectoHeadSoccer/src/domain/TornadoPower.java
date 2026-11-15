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
        super("Tornado", 40.0, 3.0, PlayerEffect.KNOCKED_OUT);
    }

    /**
     * Launches the ball forward with strong downward bounce velocity.
     *
     * @param ball Ball in play
     * @param side Side of the shooting player
     */
    @Override
    public void affectBall(Ball ball, Side side) {
        if (ball == null) return;
        double dir = (side == Side.LEFT) ? 1.0 : -1.0;
        ball.setHorizontalSpeed(17.0 * dir);
        ball.setVerticalSpeed(14.0);
    }
}
