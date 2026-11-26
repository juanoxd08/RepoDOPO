package domain;

import java.io.Serializable;

/**
 * Represents the rectangular torso/body of a player.
 * 
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class Body implements Serializable {
	private static final long serialVersionUID = 1L;
	private Rectangle rectangle;

	/**
	 * Constructs the body aligned to the player's horizontal coordinate and side.
	 * 
	 * @param playerX Player horizontal center position
	 */
	public Body(double playerX) {
		double width = 18;
		double height = 16;
		double bodyX = playerX - (width / 2.0);
		double bodyY = Field.HEIGHT - 26;
		this.rectangle = new Rectangle(bodyX, bodyY, width, height);
	}

	/**
	 * Gets the rectangular geometry of the body for domain physics.
	 *
	 * @return Rectangle geometry
	 */
	Rectangle getRectangle() {
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
