package domain;

/**
 * Immutable collision contact describing the outward normal from the obstacle
 * toward the ball and the overlap penetration depth.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class Contact {
    private final Vector2 normal;
    private final double penetration;

    /**
     * Constructs a collision contact with a normal vector and penetration depth.
     *
     * @param normal Outward unit normal vector from the obstacle toward the ball
     * @param penetration Overlap distance in pixels
     */
    public Contact(Vector2 normal, double penetration) {
        this.normal = normal;
        this.penetration = penetration;
    }

    /**
     * Returns the outward normal vector of this contact.
     *
     * @return Normal vector
     */
    public Vector2 getNormal() {
        return normal;
    }

    /**
     * Returns the overlap penetration depth in pixels.
     *
     * @return Penetration depth
     */
    public double getPenetration() {
        return penetration;
    }
}
