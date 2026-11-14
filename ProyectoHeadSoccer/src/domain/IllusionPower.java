package domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Tiro de poder de Estados Unidos: Ilusión.
 * Cómo sale: El balón se divide en 8 balones idénticos; 7 son falsos y desaparecen al terminar el efecto.
 * Efecto: Confusión (el rival no sabe cuál es el balón real. Los 8 balones se ven igual para ambos jugadores por 2.5 s).
 * DañoTiro: 30 (Daño real 6 * 5 = 30).
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class IllusionPower extends Power {
    private List<Circle> fakeBalls;
    private int illusionTicksRemaining;

    public IllusionPower() {
        super("Ilusión", 30.0, 2.5, PlayerEffect.CONFUSED);
        this.fakeBalls = new ArrayList<>();
        this.illusionTicksRemaining = 0;
    }

    @Override
    public void affectBall(Ball ball, Side side) {
        if (ball == null) return;
        double dir = (side == Side.LEFT) ? 1.0 : -1.0;
        ball.setHorizontalSpeed(18.0 * dir);
        ball.setVerticalSpeed(-5.0);

        fakeBalls.clear();
        illusionTicksRemaining = (int) (effectDurationSeconds * 60);

        // Generar 7 balones falsos alrededor
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

    public List<Circle> getFakeBalls() {
        return fakeBalls;
    }

    public boolean hasActiveIllusion() {
        return illusionTicksRemaining > 0 && !fakeBalls.isEmpty();
    }
}
