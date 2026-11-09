package domain;

/**
 * Represents the player's foot entity for kicking and ball collisions.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class Foot implements Collidable {
    private Circle circle;
    private boolean isKicking;
    private int powerKicking;
    private int kickTicks;

    /**
     * Default constructor for foot.
     */
    public Foot() {
        this.circle = new Circle();
        this.powerKicking = 22;
    }

    /**
     * Constructs a foot at specified coordinates and radius.
     * 
     * @param x Center horizontal coordinate
     * @param y Center vertical coordinate
     * @param radius Foot circle radius
     */
    public Foot(double x, double y, double radius) {
        this.circle = new Circle(x, y, radius);
        this.powerKicking = 22;
    }

    /**
     * Constructs a foot aligned to the ground and oriented based on player side.
     * 
     * @param playerX Player horizontal center position
     * @param side Player field side
     */
    public Foot(double playerX, Side side) {
        double radius = 12;
        double footX = (side == Side.LEFT) ? (playerX + 8) : (playerX - 8);
        double footY = Field.HEIGHT - radius;
        this.circle = new Circle(footX, footY, radius);
        this.powerKicking = 22;
    }

    /**
     * Gets the circle geometry of the foot.
     * 
     * @return Foot circle geometry
     */
    public Circle getCircle() {
        return circle;
    }

    /**
     * Sets the circle geometry of the foot.
     * 
     * @param circle New circle geometry
     */
    public void setCircle(Circle circle) {
        this.circle = circle;
    }

    /**
     * Checks if the foot is currently executing an active kick.
     * 
     * @return True if kicking, false otherwise
     */
    public boolean isKicking() {
        return isKicking;
    }

    /**
     * Gets the effective impact power of the kick.
     * 
     * @return Kick power impulse
     */
    public int getPowerKicking() {
        if (isKicking) {
            return powerKicking;
        } else {
            return (int) (powerKicking * 0.4);
        }
    }

    /**
     * Sets the base kick power impulse.
     * 
     * @param powerKicking New base kick power
     */
    public void setPowerKicking(int powerKicking) {
        this.powerKicking = powerKicking;
    }

    /**
     * Sets the kicking state of the foot.
     * 
     * @param isKicking New kicking state
     */
    public void setKicking(boolean isKicking) {
        this.isKicking = isKicking;
    }

    /**
     * Initiates kicking motion oriented according to player side.
     * 
     * @param side Player field side
     */
    public void kicking(Side side) {
        if (!isKicking) {
            setKicking(true);
            kickTicks = (int) (Physics.seconds(0.2));
            double dx = (side == Side.LEFT) ? 25 : -25;
            circle.translate(dx, -25);
        }
    }

    /**
     * Stops the kicking motion and restores the foot to resting position.
     * 
     * @param side Player field side
     */
    public void stopKicking(Side side) {
        if (isKicking) {
            setKicking(false);
            kickTicks = 0;
            double dx = (side == Side.LEFT) ? -25 : 25;
            circle.translate(dx, 25);
        }
    }

    /**
     * Updates the kick timer on each frame.
     * 
     * @param side Player field side
     */
    public void tick(Side side) {
        if (isKicking) {
            kickTicks--;
            if (kickTicks <= 0) {
                stopKicking(side);
            }
        }
    }
}
