package domain;

/**
 * Represents the player's head entity for heading the ball and managing stun effects.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class Head implements Collidable {
	private Circle circle;
	private int kicksRecieved;
	private int kickTicks;
	private boolean isStunned;
	private boolean struck;

	/**
	 * Default constructor for head.
	 */
	public Head() {
		this.circle = new Circle();
	}

	/**
	 * Constructs a head at specified center coordinates and radius.
	 * 
	 * @param x Center horizontal coordinate
	 * @param y Center vertical coordinate
	 * @param radius Head circle radius
	 */
	public Head(double x, double y, double radius) {
		this.circle = new Circle(x, y, radius);
	}

	/**
	 * Constructs a head aligned to the initial height and side of the player.
	 * 
	 * @param playerX Player horizontal center position
	 * @param side Player field side
	 */
	public Head(double playerX, Side side) {
		double radius = 20;
		double headY = Field.HEIGHT - 55;
		this.circle = new Circle(playerX, headY, radius);
	}

	/**
	 * Gets the circle geometry defining the head.
	 * 
	 * @return Circle geometry
	 */
	public Circle getCircle() {
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

	/**
	 * Gets the consecutive kick count received by the head.
	 * 
	 * @return Kick count
	 */
	public int getKicksRecieved() {
		return kicksRecieved;
	}

	/**
	 * Sets the consecutive kick count received.
	 * 
	 * @param kicksRecieved New kick count
	 */
	public void setKicksRecieved(int kicksRecieved) {
		this.kicksRecieved = kicksRecieved;
	}

	/**
	 * Indicates whether the head is currently stunned.
	 * 
	 * @return True if stunned, false otherwise
	 */
	public boolean isStunned() {
		return isStunned;
	}

	/**
	 * Modifies stun state and initializes 3-second stun timer if activated.
	 * 
	 * @param isStunned New stun state
	 */
	public void setStunned(boolean isStunned) {
		this.isStunned = isStunned;
		if (isStunned && this.kickTicks <= 0) {
			this.kickTicks = (int) (Physics.seconds(3));
		}
	}

	@Override
	public void collideWithFoot(Foot foot) {
		if (foot == null || foot.getCircle() == null) return;
		boolean kicking = foot.isKicking();
		if (kicking && circle.detectCollisionWithCircle(foot.getCircle())) {
			if (!struck) {
				struck = true;
				kicksRecieved++;
				isStruck(foot);
			}
			if (kicksRecieved >= 5) {
				kicksRecieved = 0;
				kickTicks = Physics.seconds(3.0);
				isStunned = true;
			}
		}
	}

	private void isStruck(Foot foot) {
		if (getCircle().getX() > foot.getCircle().getX()) {
			getCircle().translate(8.0, -10.0);
		} else {
			getCircle().translate(-8.0, -10.0);
		}
	}

	public void isNotStruck(Foot foot) {
		if (struck) {
			if (getCircle().getX() > foot.getCircle().getX()) {
				getCircle().translate(-8.0, 10.0);
			} else {
				getCircle().translate(8.0, 10.0);
			}
			struck = false;
		}
	}

	public void tick() {
		if (isStunned) {
			kickTicks--;
			if (kickTicks <= 0) {
				isStunned = false;
			}
		}
	}
}
