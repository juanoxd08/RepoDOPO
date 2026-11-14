package domain;

/**
 * Tiro de poder de Corea del Sur: Aura Azul.
 * Cómo sale: Recto y horizontal, a alta velocidad.
 * Efecto: Empuje (el rival es lanzado hacia su propio arco por 1.5 s).
 * DañoTiro: 40 (Daño real 8 * 5 = 40).
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class AuraAzulPower extends Power {

    public AuraAzulPower() {
        super("Aura Azul", 40.0, 1.5, PlayerEffect.PUSHED);
    }

    @Override
    public void affectBall(Ball ball, Side side) {
        if (ball == null) return;
        double dir = (side == Side.LEFT) ? 1.0 : -1.0;
        // Recto y horizontal a muy alta velocidad
        ball.setHorizontalSpeed(24.0 * dir);
        ball.setVerticalSpeed(-1.5);
    }
}
