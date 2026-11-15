package domain;

/**
 * Immutable 2D vector used for positions, velocities, and collision normals.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class Vector2 {
    private final double x;
    private final double y;

    /**
     * Constructs a 2D vector.
     *
     * @param x Horizontal component
     * @param y Vertical component
     */
    public Vector2(double x, double y){
        this.x = x;
        this.y = y;
    }

    /** @return Horizontal component */
    public double getX(){
        return x;
    }

    /** @return Vertical component */
    public double getY(){
        return y;
    }

    /** @return Sum of this vector and {@code other} */
    public Vector2 add(Vector2 other){
        return new Vector2(this.x + other.getX(), this.y + other.getY());
    }

    /** @return Difference of this vector minus {@code other} */
    public Vector2 sub(Vector2 other){
        return new Vector2(this.x - other.getX(), this.y - other.getY());
    }

    /** @return Scaled vector */
    public Vector2 scale(double factor){
        return new Vector2(this.x * factor, this.y * factor);
    }

    /** @return Dot product with {@code other} */
    public double dot(Vector2 other){
        return this.x*other.getX() + this.y*other.getY();
    }

    /** @return Euclidean length */
    public double length(){
        return Math.hypot(x, y);
    }

    /** @return Unit vector in the same direction, or zero vector if length is 0 */
    public Vector2 normalized(){
        double len = length();
        return (len == 0) ? new Vector2(0, 0) : scale(1.0 / len);
    }

}
