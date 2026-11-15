package domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Tiro de poder de Estados Unidos: IlusiÃ³n.
 * CÃ³mo sale: El balÃ³n se divide en 8 balones idÃ©nticos; 7 son falsos y desaparecen al terminar el efecto.
 * Efecto: ConfusiÃ³n (el rival no sabe cuÃ¡l es el balÃ³n real. Los 8 balones se ven igual para ambos jugadores por 2.5 s).
 * DaÃ±oTiro: 30 (DaÃ±o real 6 * 5 = 30).
 * 
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class IllusionPower extends Power {
    private List<Circle> fakeBalls;
    private int illusionTicksRemaining;

    /**
     * Constructs USA's Illusion power.
     */
    public IllusionPower() {
        super("IlusiÃ³n", 30.0, 2.5, PlayerEffect.CONFUSED);
        this.fakeBalls = new ArrayList<>();
        this.illusionTicksRemaining = 0;
    }

    /**
     * Launches the ball and spawns 7 illusionary decoy balls around it.
     *
     * @param ball Ball in play
     * @param side Side of the shooting player
     */
    @Override
    public void affectBall(Ball ball, Side side) {
        if (ball == null) return;
        double dir = (side == Side.LEFT) ? 1.0 : -1.0;
        ball.setHorizontalSpeed(18.0 * dir);
        ball.setVerticalSpeed(-5.0);

        fakeBalls.clear();
        illusionTicksRemaining = (int) (effectDurationSeconds * 60);

        double bx = ball.getCircle().getX();
        double by = ball.getCircle().getY();
        double r = ball.getCircle().getRadius();

        for (int i = 0; i < 7; i++) {
            double angle = (i * (2 * Math.PI / 7));
            double fx = bx + Math.cos(angle) * (18 + i * 4);
            double fy = by + Math.sin(angle) * (15 + i * 3);
            fakeBalls.add(new Circle(fx, fy, r));
        }
    }

    /**
     * Updates illusion clones movement following the real ball with slight offsets.
     * 
     * @param realBall Main game ball
     */
    public void updateClones(Ball realBall) {
        if (illusionTicksRemaining > 0 && realBall != null) {
            illusionTicksRemaining--;
            double bx = realBall.getCircle().getX();
            double by = realBall.getCircle().getY();
            for (int i = 0; i < fakeBalls.size(); i++) {
                Circle c = fakeBalls.get(i);
                double offsetAngle = (i * 0.9) + (illusionTicksRemaining * 0.1);
                c.setX(bx + Math.cos(offsetAngle) * (20 + (i % 3) * 8));
                c.setY(by + Math.sin(offsetAngle) * (14 + (i % 2) * 6));
            }
        } else {
            fakeBalls.clear();
        }
    }

    /**
     * Returns the list of active illusionary decoy circles.
     *
     * @return List of fake ball circles
     */
    public List<Circle> getFakeBalls() {
        return fakeBalls;
    }

    /**
     * Checks whether illusion decoy balls are currently active.
     *
     * @return True if illusion is active
     */
    public boolean hasActiveIllusion() {
        return illusionTicksRemaining > 0 && !fakeBalls.isEmpty();
    }

    /**
     * Updates decoy clone positions during each simulation tick.
     *
     * @param ball Real ball in play
     */
    @Override
    public void updateShot(Ball ball){
        updateClones(ball);
    }
}
