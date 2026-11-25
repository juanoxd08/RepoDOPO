package domain;

import java.io.Serializable;

/**
 * Represents the player's head entity for heading the ball and managing stun effects.
 * 
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class Head implements Serializable {
	private static final long serialVersionUID = 1L;
	private Circle circle;

	/**
	 * Constructs a head aligned to the initial height and side of the player.
	 *
	 * @param playerX Player horizontal center position
	 * @param side Player field side
	 */
	public Head(double playerX, Side side) {
		double radius = 14;
		double headY = Field.HEIGHT - 38;
		this.circle = new Circle(playerX, headY, radius);
	}

	/**
	 * Gets the circle geometry defining the head for domain physics.
	 *
	 * @return Circle geometry
	 */
	Circle getCircle() {
		return circle;
	}

	/**
	 * Sets the circle geometry of the head.
	 *
	 * @param circle New circle geometry
	 */
	public void setCircle(Circle circle) {
		this.circle = circle;
	}
}
