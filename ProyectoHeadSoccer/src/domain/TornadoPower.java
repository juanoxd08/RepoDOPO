package domain;

/**
 * Tiro de poder de Nigeria: Tornado.
 * Cómo sale: Remolino a ras de suelo que rebota rápidamente.
 * Efecto: Inconsciente (es lanzado al aire y no puede moverse, saltar ni patear por 3 s).
 * DañoTiro: 40 (Daño real 8 * 5 = 40).
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class TornadoPower extends Power {

    public TornadoPower() {
        super("Tornado", 40.0, 3.0, PlayerEffect.KNOCKED_OUT);
    }

    @Override
    public void affectBall(Ball ball, Side side) {
        if (ball == null) return;
        double dir = (side == Side.LEFT) ? 1.0 : -1.0;
        // A ras de suelo rebotando rápidamente
        ball.setHorizontalSpeed(17.0 * dir);
        ball.setVerticalSpeed(14.0);
    }
}
