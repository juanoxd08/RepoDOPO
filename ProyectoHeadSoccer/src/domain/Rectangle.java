package domain;

/**
 * Axis-aligned rectangular geometric shape supporting bounding box collision and containment tests.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class Rectangle extends Figure {
	private double width;
	private double height;

	/**
	 * Default constructor initializing rectangle at origin with zero dimensions.
	 */
	public Rectangle() {
		super();
	}

	/**
	 * Constructs a rectangle with specified top-left coordinates and dimensions.
	 * 
	 * @param x Top-left horizontal coordinate
	 * @param y Top-left vertical coordinate
	 * @param width Horizontal dimension
	 * @param height Vertical dimension
	 */
	public Rectangle(double x, double y, double width, double height) {
		super(x, y);
		this.width = width;
		this.height = height;
	}

	/**
	 * Returns the width dimension.
	 * 
	 * @return Rectangle width
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * Sets the width dimension.
	 * 
	 * @param width Rectangle width to set
	 */
	public void setWidth(double width) {
		this.width = width;
	}

	/**
	 * Returns the height dimension.
	 * 
	 * @return Rectangle height
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * Sets the height dimension.
	 * 
	 * @param height Rectangle height to set
	 */
	public void setHeight(double height) {
		this.height = height;
	}

	/**
	 * Returns the leftmost X boundary coordinate.
	 * 
	 * @return Left X coordinate
	 */
	public double left() {
		return getX();
	}

	/**
	 * Returns the rightmost X boundary coordinate.
	 * 
	 * @return Right X coordinate
	 */
	public double right() {
		return getX() + width;
	}

	/**
	 * Returns the topmost Y boundary coordinate.
	 * 
	 * @return Top Y coordinate
	 */
	public double top() {
		return getY();
	}

	/**
	 * Returns the bottommost Y boundary coordinate.
	 * 
	 * @return Bottom Y coordinate
	 */
	public double bottom() {
		return getY() + height;
	}

	/**
	 * Detects collision between this rectangle and a given circle.
	 * 
	 * @param circle Target circle to test
	 * @return True if intersecting, false otherwise
	 */
	@Override
	public boolean detectCollisionWithCircle(Circle circle) {
		double closestX = Math.max(left(), Math.min(circle.getX(), right()));
		double closestY = Math.max(top(), Math.min(circle.getY(), bottom()));
		double dx = circle.getX() - closestX;
		double dy = circle.getY() - closestY;
		return (dx * dx) + (dy * dy) <= (circle.getRadius() * circle.getRadius());
	}

	/**
	 * Detects collision between this rectangle and another rectangle (AABB overlap).
	 * 
	 * @param rectangle Target rectangle to test
	 * @return True if overlapping, false otherwise
	 */
	@Override
	public boolean detectCollisionWithRectangle(Rectangle rectangle) {
		return rectangle.right() >= this.left()
				&& rectangle.left() <= this.right()
				&& rectangle.bottom() >= this.top()
				&& rectangle.top() <= this.bottom();
	}

	/**
	 * Checks whether a given circle is completely enclosed inside this rectangle.
	 * 
	 * @param circle Target circle to test
	 * @return True if fully contained, false otherwise
	 */
	public boolean containsCircle(Circle circle) {
		return circle.getX() - circle.getRadius() >= left()
				&& circle.getX() + circle.getRadius() <= right()
				&& circle.getY() - circle.getRadius() >= top()
				&& circle.getY() + circle.getRadius() <= bottom();
	}
}
