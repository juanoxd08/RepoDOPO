package domain;

/**
 * Tiro de poder de CamerÃºn: Rayo.
 * CÃ³mo sale: Recto, cubierto de electricidad.
 * Efecto: Electrocutado (no puede saltar y se mueve al 50 % de su velocidad por 3 s).
 * DaÃ±oTiro: 60 (DaÃ±o real 12 * 5 = 60).
 * 
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class LightningPower extends Power {

    /**
     * Constructs Cameroon's Lightning power.
     */
    public LightningPower() {
        super("Rayo", 3.0);
    }

    /**
     * Launches the ball straight forward on a low trajectory.
     *
     * @param ball Ball in play
     * @param side Side of the shooting player
     */
    @Override
    protected void shapeShot(Ball ball, Side side) {
        if (ball == null) return;
        double dir = (side == Side.LEFT) ? 1.0 : -1.0;
        ball.setHorizontalSpeed(20.0 * dir);
        ball.setVerticalSpeed(-2.0);
    }

    /**
     * Returns the base power damage for Rayo.
     *
     * @return 60.0
     */
    @Override
    public double getBaseDamage() {
        return 60.0;
    }

    /**
     * Creates the ELECTROCUTED effect applied on impact.
     *
     * @return ElectrocutedEffect instance
     */
    @Override
    public Effect createEffect() {
        return new ElectrocutedEffect();
    }
}
