package domain;

/**
 * Tiro de poder de Canadá: Congelación.
 * Congela al oponente en un bloque de hielo durante 3 segundos.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class FreezePower extends Power {

    public FreezePower() {
        super("Congelación", 40.0, 3.0, PlayerEffect.FROZEN);
    }

    @Override
    public void affectBall(Ball ball, Side side) {
        if (ball == null) return;
        double dir = (side == Side.LEFT) ? 1.0 : -1.0;
        ball.setHorizontalSpeed(19.0 * dir);
        ball.setVerticalSpeed(-6.0);
    }
}
