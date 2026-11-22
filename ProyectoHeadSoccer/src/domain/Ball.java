package domain;

import java.io.Serializable;
import java.util.Optional;

/**
 * Represents the soccer ball with physics simulation including gravity, bouncing, and collision detection.
 * 
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class Ball implements BallView, Serializable {
	private static final long serialVersionUID = 1L;
	private Circle circle;
	private double verticalSpeed;
	private double horizontalSpeed;
	private boolean powerShot;
	private Country powerShotCountry;
	private boolean powerShotCharging;
	private double powerShotChargeProgress;
	
	/**
	 * Default constructor initializing ball at origin with default radius.
	 */
	public Ball() {
		this.circle = new Circle(0, 0, 8);
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
			if(verticalSpeed >= 5){
				setVerticalSpeed(-Math.abs(verticalSpeed) * Physics.RESTITUTION);
			}else{
				setVerticalSpeed(0.0);
			}
				
		}
	}

	public boolean bounceOff(Figure obstacule, double restitution, double impulse){
		if(obstacule == null) return false;
		Optional<Contact> opt = obstacule.contactWith(this.circle);
		if(opt.isEmpty()) return false;
		Contact c = opt.get();
		Vector2 normal = c.getNormal();

		boolean onFloor = this.getCircle().getY() + this. getCircle().getRadius() >= Field.HEIGHT - 0.5;
		if(onFloor && normal.getY() > 0){
			if(Math.abs(normal.getX()) > 0.01){
				normal = new Vector2(Math.signum(normal.getX()), 0);
			}else{
				normal = new Vector2(0, -1);
			}
		}
		this.circle.translate(normal.getX() * c.getPenetration(), normal.getY() * c.getPenetration());

		Vector2 v = new Vector2(horizontalSpeed, verticalSpeed);
		double vn = v.dot(normal);
		if (vn < 0) v = v.sub(normal.scale((1.0 + restitution) * vn));
		if(impulse > 0) v = v.add(normal.scale(impulse));
		this.horizontalSpeed = v.getX();
		this.verticalSpeed = v.getY();
		return true;
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
		copy.setPowerShot(powerShot);
		copy.setPowerShotCountry(powerShotCountry);
		copy.setPowerShotCharging(powerShotCharging, powerShotChargeProgress);
		return copy;
	}

	@Override
	public double getX() {
		return circle.getX();
	}

	@Override
	public double getY() {
		return circle.getY();
	}

	@Override
	public double getRadius() {
		return circle.getRadius();
	}

	@Override
	public boolean hasPowerShot() {
		return powerShot;
	}

	/**
	 * Returns the country that launched the active power shot, or null if none.
	 *
	 * @return Shooting country or null
	 */
	public Country getPowerShotCountry() {
		return powerShotCountry;
	}

	/**
	 * Indicates whether the ball is currently suspended while charging a power shot.
	 *
	 * @return True if charging a power shot
	 */
	public boolean isPowerShotCharging() {
		return powerShotCharging;
	}

	/**
	 * Returns the normalized charge progress [0.0, 1.0] while suspended for a power shot.
	 *
	 * @return Charge progress in [0.0, 1.0]
	 */
	public double getPowerShotChargeProgress() {
		return powerShotChargeProgress;
	}

	/**
	 * Marks whether the ball currently carries a power shot, clearing power visual state when false.
	 *
	 * @param powerShot True when a power shot starts, false to clear
	 */
	public void setPowerShot(boolean powerShot) {
		this.powerShot = powerShot;
		if (!powerShot) {
			this.powerShotCountry = null;
			this.powerShotCharging = false;
			this.powerShotChargeProgress = 0.0;
		}
	}

	/**
	 * Sets the country associated with the current power shot.
	 *
	 * @param country Country that fired the power shot
	 */
	public void setPowerShotCountry(Country country) {
		this.powerShotCountry = country;
	}

	/**
	 * Updates the power shot charging state and normalized progress [0.0, 1.0].
	 *
	 * @param charging True while suspended in windup
	 * @param progress Charge progress clamped to [0.0, 1.0]
	 */
	public void setPowerShotCharging(boolean charging, double progress) {
		this.powerShotCharging = charging;
		this.powerShotChargeProgress = charging ? Math.max(0.0, Math.min(1.0, progress)) : 0.0;
	}
}
