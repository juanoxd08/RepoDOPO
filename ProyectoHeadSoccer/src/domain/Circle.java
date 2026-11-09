package domain;

/**
 * Geometric circle figure defined by center coordinates and a radius.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class Circle extends Figure {
	private double radius;

	/**
	 * Default constructor initializing circle at origin.
	 */
	public Circle() {
		super();
	}

	/**
	 * Constructs a circle at specified center coordinates with given radius.
	 * 
	 * @param x Center horizontal coordinate
	 * @param y Center vertical coordinate
	 * @param radius Radius of the circle
	 */
	public Circle(double x, double y, double radius) {
		super(x, y);
		this.radius = radius;
	}

	/**
	 * Gets the radius of the circle.
	 * 
	 * @return Circle radius
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * Sets the radius of the circle.
	 * 
	 * @param radius New radius
	 */
	public void setRadius(double radius) {
		this.radius = radius;
	}

	/**
	 * Detects collision against another circle using Euclidean distance squared.
	 * 
	 * @param circle Target circle
	 * @return True if circles intersect, false otherwise
	 */
	@Override
	public boolean detectCollisionWithCircle(Circle circle) {
		double dx = this.getX() - circle.getX();
		double dy = this.getY() - circle.getY();
		double radiusSum = this.radius + circle.radius;
		return (dx * dx) + (dy * dy) <= (radiusSum * radiusSum);
	}

	/**
	 * Detects collision against a rectangle by delegating to the rectangle's circle collision method.
	 * 
	 * @param rectangle Target rectangle
	 * @return True if circle intersects rectangle, false otherwise
	 */
	@Override
	public boolean detectCollisionWithRectangle(Rectangle rectangle) {
		return rectangle.detectCollisionWithCircle(this);
	}
}
