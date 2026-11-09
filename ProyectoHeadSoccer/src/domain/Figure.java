package domain;

/**
 * Abstract base class for 2D geometric shapes with Cartesian position and collision detection.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public abstract class Figure {
	private double x;
	private double y;
	
	/**
	 * Default constructor initializing figure at origin.
	 */
	public Figure() {
	}

	/**
	 * Constructs a figure at specified coordinates.
	 * 
	 * @param x Horizontal coordinate
	 * @param y Vertical coordinate
	 */
	public Figure(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Gets the horizontal coordinate.
	 * 
	 * @return Horizontal position
	 */
	public double getX() {
		return x;
	}
	
	/**
	 * Sets the horizontal coordinate.
	 * 
	 * @param x New horizontal position
	 */
	public void setX(double x) {
		this.x = x;
	}
	
	/**
	 * Gets the vertical coordinate.
	 * 
	 * @return Vertical position
	 */
	public double getY() {
		return y;
	}
	
	/**
	 * Sets the vertical coordinate.
	 * 
	 * @param y New vertical position
	 */
	public void setY(double y) {
		this.y = y;
	}
	
	/**
	 * Translates figure coordinates by given displacements.
	 * 
	 * @param dx Horizontal displacement
	 * @param dy Vertical displacement
	 */
	public void translate(double dx, double dy) {
		this.x += dx;
		this.y += dy;
	}
	
	/**
	 * Detects collision against a circle figure.
	 * 
	 * @param circle Target circle
	 * @return True if colliding, false otherwise
	 */
	public abstract boolean detectCollisionWithCircle(Circle circle);

	/**
	 * Detects collision against a rectangle figure.
	 * 
	 * @param rectangle Target rectangle
	 * @return True if colliding, false otherwise
	 */
	public abstract boolean detectCollisionWithRectangle(Rectangle rectangle);
}
