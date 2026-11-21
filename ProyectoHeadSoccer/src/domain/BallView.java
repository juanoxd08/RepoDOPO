package domain;

/**
 * Read-only view of the ball state for presentation and AI controllers.
 * 
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public interface BallView {
    double getX();
    double getY();
    double getRadius();
    boolean hasPowerShot();
    double getHorizontalSpeed();
    double getVerticalSpeed();
}
