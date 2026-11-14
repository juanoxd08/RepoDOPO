package domain;

/**
 * Read-only view of the ball state for presentation and AI controllers.
 */
public interface BallView {
    double getX();
    double getY();
    double getRadius();
    boolean hasPowerShot();
}
