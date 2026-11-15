package domain;

/**
 * Immutable collision contact describing the outward normal from the obstacle
 * toward the ball and the overlap penetration depth.
 */
public class Contact {
    private final Vector2 normal;
    private final double penetration;

    public Contact(Vector2 normal, double penetration) {
        this.normal = normal;
        this.penetration = penetration;
    }

    public Vector2 getNormal() {
        return normal;
    }

    public double getPenetration() {
        return penetration;
    }
}
