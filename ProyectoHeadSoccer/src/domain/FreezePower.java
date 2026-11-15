package domain;

/**
 * Tiro de poder de CanadÃ¡: CongelaciÃ³n.
 * Congela al oponente en un bloque de hielo durante 3 segundos.
 * 
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class FreezePower extends Power {

    /**
     * Constructs Canada's Freeze power.
     */
    public FreezePower() {
        super("Congelación", 40.0, 3.0, PlayerEffect.FROZEN);
    }

    /**
     * Launches the ball forward with an upward arc.
     *
     * @param ball Ball in play
     * @param side Side of the shooting player
     */
    @Override
    public void affectBall(Ball ball, Side side) {
        if (ball == null) return;
        double dir = (side == Side.LEFT) ? 1.0 : -1.0;
        ball.setHorizontalSpeed(19.0 * dir);
        ball.setVerticalSpeed(-6.0);
    }
}
