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
        super("Congelación", 3.0);
    }

    /**
     * Launches the ball forward with an upward arc.
     *
     * @param ball Ball in play
     * @param side Side of the shooting player
     */
    @Override
    public void shapeShot(Ball ball, Side side) {
        if (ball == null) return;
        double dir = (side == Side.LEFT) ? 1.0 : -1.0;
        ball.setHorizontalSpeed(19.0 * dir);
        ball.setVerticalSpeed(-6.0);
    }

    /**
     * Returns the base power damage for Congelación.
     *
     * @return 40.0
     */
    @Override 
    public double getBaseDamage(){
        return 40.0;
    }

    /**
     * Creates the FROZEN effect applied on impact.
     *
     * @return FrozenEffect instance
     */
    @Override 
    public Effect createEffect(){
        return new FrozenEffect();
    }
}
