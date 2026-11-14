package domain;

/**
 * Tiro de poder de Camerún: Rayo.
 * Cómo sale: Recto, cubierto de electricidad.
 * Efecto: Electrocutado (no puede saltar y se mueve al 50 % de su velocidad por 3 s).
 * DañoTiro: 60 (Daño real 12 * 5 = 60).
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class LightningPower extends Power {

    public LightningPower() {
        super("Rayo", 60.0, 3.0, PlayerEffect.ELECTROCUTED);
    }

    @Override
    public void affectBall(Ball ball, Side side) {
        if (ball == null) return;
        double dir = (side == Side.LEFT) ? 1.0 : -1.0;
        // Recto eléctrico a gran potencia
        ball.setHorizontalSpeed(20.0 * dir);
        ball.setVerticalSpeed(-2.0);
    }
}
