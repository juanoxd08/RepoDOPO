package domain;

/**
 * Represents the rectangular torso/body of a player.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class Body implements Collidable {
	private Rectangle rectangle;

	/**
	 * Default constructor.
	 */
	public Body() {
		this.rectangle = new Rectangle();
	}

	/**
	 * Constructs the body with specified coordinates and dimensions.
	 * 
	 * @param x Top-left horizontal coordinate
	 * @param y Top-left vertical coordinate
	 * @param width Rectangle width
	 * @param height Rectangle height
	 */
	public Body(double x, double y, double width, double height) {
		this.rectangle = new Rectangle(x, y, width, height);
	}

	/**
	 * Constructs the body aligned to the player's horizontal coordinate and side.
	 * 
	 * @param playerX Player horizontal center position
	 * @param side Side of the field the player defends
	 */
	public Body(double playerX, Side side) {
		double width = 26;
		double height = 22;
		double bodyX = playerX - (width / 2.0);
		double bodyY = Field.HEIGHT - 38;
		this.rectangle = new Rectangle(bodyX, bodyY, width, height);
	}

	/**
	 * Gets the rectangular geometry of the body.
	 * 
	 * @return Rectangle geometry
	 */
	public Rectangle getRectangle() {
		return rectangle;
	}

	/**
	 * Sets the rectangular geometry of the body.
	 * 
	 * @param rectangle New rectangle geometry
	 */
	public void setRectangle(Rectangle rectangle) {
		this.rectangle = rectangle;
	}
}
