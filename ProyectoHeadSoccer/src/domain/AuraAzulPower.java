package domain;

/**
 * Tiro de poder de Corea del Sur: Aura Azul.
 * CÃ³mo sale: Recto y horizontal, a alta velocidad.
 * Efecto: Empuje (el rival es lanzado hacia su propio arco por 1.5 s).
 * DaÃ±oTiro: 40 (DaÃ±o real 8 * 5 = 40).
 * 
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class AuraAzulPower extends Power {

    /**
     * Constructs South Korea's Aura Azul power.
     */
    public AuraAzulPower() {
        super("Aura Azul", 40.0, 1.5, PlayerEffect.PUSHED);
    }

    /**
     * Launches the ball horizontally at high speed toward the opponent's side.
     *
     * @param ball Ball in play
     * @param side Side of the shooting player
     */
    @Override
    public void affectBall(Ball ball, Side side) {
        if (ball == null) return;
        double dir = (side == Side.LEFT) ? 1.0 : -1.0;
        ball.setHorizontalSpeed(24.0 * dir);
        ball.setVerticalSpeed(-1.5);
    }
}
