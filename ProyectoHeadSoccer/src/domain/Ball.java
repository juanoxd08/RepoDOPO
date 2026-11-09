package domain;

/**
 * Represents the soccer ball with physics simulation including gravity, bouncing, and collision detection.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class Ball implements Collidable {
	private Circle circle;
	private double verticalSpeed;
	private double horizontalSpeed;
	
	/**
	 * Default constructor initializing ball at origin with default radius.
	 */
	public Ball() {
		this.circle = new Circle(0, 0, 10);
	}

	/**
	 * Constructs a ball at specified coordinates with given radius.
	 * 
	 * @param x Initial horizontal center position
	 * @param y Initial vertical center position
	 * @param radius Radius of the ball
	 */
	public Ball(double x, double y, double radius) {
		this.circle = new Circle(x, y, radius);
	}

	/**
	 * Gets the current vertical velocity.
	 * 
	 * @return Vertical speed in pixels per tick
	 */
	public double getVerticalSpeed() {
		return verticalSpeed;
	}
	
	/**
	 * Sets the vertical velocity.
	 * 
	 * @param verticalSpeed New vertical speed
	 */
	public void setVerticalSpeed(double verticalSpeed) {
		this.verticalSpeed = verticalSpeed;
	}
	
	/**
	 * Gets the current horizontal velocity.
	 * 
	 * @return Horizontal speed in pixels per tick
	 */
	public double getHorizontalSpeed() {
		return horizontalSpeed;
	}
	
	/**
	 * Sets the horizontal velocity.
	 * 
	 * @param horizontalSpeed New horizontal speed
	 */
	public void setHorizontalSpeed(double horizontalSpeed) {
		this.horizontalSpeed = horizontalSpeed;
	}
	
	/**
	 * Gets the circle geometric representation of the ball.
	 * 
	 * @return Circle geometry
	 */
	public Circle getCircle() {
		return circle;
	}

	/**
	 * Sets the circle geometry of the ball.
	 * 
	 * @param circle New circle geometry
	 */
	public void setCircle(Circle circle) {
		this.circle = circle;
	}

	/**
	 * Handles collision impulse against a player's foot.
	 * 
	 * @param foot Foot entity collided with
	 */
	@Override
	public void collideWithFoot(Foot foot) {
		if (foot == null || foot.getCircle() == null) return;
		if (!this.circle.detectCollisionWithCircle(foot.getCircle())) return;

		double dx = this.circle.getX() - foot.getCircle().getX();
		double dy = this.circle.getY() - foot.getCircle().getY();
		double distance = Math.hypot(dx, dy);
		if (distance == 0) {
			dx = 1;
			dy = -1;
			distance = Math.hypot(dx, dy);
		}
		int powerKick = foot.getPowerKicking();
		setHorizontalSpeed((dx / distance) * powerKick);
		setVerticalSpeed((dy / distance) * powerKick);
	}
	
	/**
	 * Resolves physical bounce against a rectangular obstacle such as crossbar or player body.
	 * 
	 * @param r Rectangle entity collided with
	 */
	public void collideWithGB(Rectangle r) {
		if (r == null || !r.detectCollisionWithCircle(this.circle)) return;

		if (circle.getX() >= r.left() && circle.getX() <= r.right()) {
			setVerticalSpeed(-verticalSpeed * Physics.RESTITUTION);
		} else if (circle.getY() >= r.top() && circle.getY() <= r.bottom()) {
			setHorizontalSpeed(-horizontalSpeed * Physics.RESTITUTION);
		} else {
			double cornerX = (circle.getX() < r.left()) ? r.left() : r.right();
			double cornerY = (circle.getY() < r.top()) ? r.top() : r.bottom();
			double dx = circle.getX() - cornerX;
			double dy = circle.getY() - cornerY;
			double distance = Math.hypot(dx, dy);
			if (distance == 0) {
				dx = 0;
				dy = -1;
				distance = 1;
			}
			double speed = Math.hypot(horizontalSpeed, verticalSpeed) * Physics.CORNER_RESTITUTION;
			setHorizontalSpeed((dx / distance) * speed);
			setVerticalSpeed((dy / distance) * speed);
		}
	}
	
	/**
	 * Handles collision and header impulse against a player's head.
	 * 
	 * @param head Head entity collided with
	 */
	public void collideWithHead(Head head) {
		if (head == null || head.getCircle() == null) return;
		if (!this.circle.detectCollisionWithCircle(head.getCircle())) return;

		double dx = this.circle.getX() - head.getCircle().getX();
		double dy = this.circle.getY() - head.getCircle().getY();
		double distance = Math.hypot(dx, dy);
		if (distance == 0) {
			dx = 0;
			dy = -1;
			distance = 1;
		}
		setHorizontalSpeed((dx / distance) * 20);
		setVerticalSpeed((dy / distance) * 20);	
	}
	
	/**
	 * Resolves bounces against field boundary edges (floor, ceiling, walls).
	 */
	public void collideWithEdges() {
		if (this.circle.getX() - this.circle.getRadius() <= 0) {
			this.circle.setX(this.circle.getRadius());
			setHorizontalSpeed(Math.abs(horizontalSpeed) * Physics.RESTITUTION);
		}
		if (this.circle.getX() + this.circle.getRadius() >= Field.WIDTH) {
			this.circle.setX(Field.WIDTH - this.circle.getRadius());
			setHorizontalSpeed(-Math.abs(horizontalSpeed) * Physics.RESTITUTION);
		}
		if (this.circle.getY() - this.circle.getRadius() <= 0) {
			this.circle.setY(this.circle.getRadius());
			setVerticalSpeed(Math.abs(verticalSpeed) * Physics.RESTITUTION);
		}
		if (this.circle.getY() + this.circle.getRadius() >= Field.HEIGHT) {
			this.circle.setY(Field.HEIGHT - this.circle.getRadius());
			setVerticalSpeed(-Math.abs(verticalSpeed) * Physics.RESTITUTION);
		}
	}
	
	/**
	 * Advances ball physics simulation by one frame, applying gravity and ground friction.
	 */
	public void tick() {
		circle.translate(horizontalSpeed, verticalSpeed);
		
		if (this.circle.getY() + this.circle.getRadius() < Field.HEIGHT) { 
			verticalSpeed += Physics.GRAVITY;
		} else {
			this.circle.setY(Field.HEIGHT - this.circle.getRadius());
			horizontalSpeed = horizontalSpeed * Physics.FRICTION;
			if (Math.abs(horizontalSpeed) < 0.05) {
				horizontalSpeed = 0;
			}
		}
	}

	/**
	 * Resets ball position and zeroes velocities.
	 * 
	 * @param x Target center horizontal position
	 * @param y Target center vertical position
	 */
	public void reset(double x, double y) {
		getCircle().setX(x);
		getCircle().setY(y);
		setHorizontalSpeed(0);
		setVerticalSpeed(0);
	}

	/**
	 * Creates a deep copy of the ball state.
	 * 
	 * @return Cloned ball instance
	 */
	public Ball copy() {
		Ball copy = new Ball(this.circle.getX(), this.circle.getY(), this.circle.getRadius());
		copy.setHorizontalSpeed(horizontalSpeed);
		copy.setVerticalSpeed(verticalSpeed);
		return copy;
	}
}
