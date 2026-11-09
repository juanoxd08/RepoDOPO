package domain;

import java.util.Set;

/**
 * Represents a soccer player entity composed of head, body, and foot limbs, with movement and physics simulation.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class Player implements Collidable {
	private String name;
	private Country country;
	private Side side;
	private Power power; 
	private Head head;
	private Body body;
	private Foot foot;
	private PlayerStats stats;
	private double verticalSpeed;
	private boolean onTop;
	private int points;

	/**
	 * Default constructor initializing default player values.
	 */
	public Player() {
		this.verticalSpeed = 0;
		this.onTop = false;
		this.points = 0;
	}

	/**
	 * Constructs a player with specified profile attributes, pitch side, special power, and stats.
	 * 
	 * @param name Name of the player
	 * @param country Country representation
	 * @param side Pitch side assigned
	 * @param power Special power ability
	 * @param stats Player performance stats
	 */
	public Player(String name, Country country, Side side, Power power, PlayerStats stats) {
		double initialX = (side == Side.LEFT) ? 100 : Field.WIDTH - 100;
		this.name = name;
		this.country = country;
		this.side = side;
		this.power = power;
		this.stats = stats;
		this.verticalSpeed = 0;
		this.onTop = false;
		this.points = 0;
		this.head = new Head(initialX, side);
		this.body = new Body(initialX, side);
		this.foot = new Foot(initialX, side);
		if (this.stats != null) {
			this.foot.setPowerKicking((int) this.stats.kickPx());
		}
	}

	/**
	 * Resets player position and vertical state to kickoff coordinates.
	 */
	public void resetPosition() {
		double initialX = (side == Side.LEFT) ? 100 : Field.WIDTH - 100;
		this.verticalSpeed = 0;
		this.onTop = false;
		this.head = new Head(initialX, side);
		this.body = new Body(initialX, side);
		this.foot = new Foot(initialX, side);
		if (this.stats != null) {
			this.foot.setPowerKicking((int) this.stats.kickPx());
		}
	}

	/**
	 * Translates all limbs of the player along Cartesian axes.
	 * 
	 * @param dx Horizontal offset in pixels
	 * @param dy Vertical offset in pixels
	 */
	public void translate(double dx, double dy) {
		if (head != null && head.getCircle() != null) {
			head.getCircle().translate(dx, dy);
		}
		if (body != null && body.getRectangle() != null) {
			body.getRectangle().translate(dx, dy);
		}
		if (foot != null && foot.getCircle() != null) {
			foot.getCircle().translate(dx, dy);
		}
	}

	/**
	 * Moves player leftwards according to speed attribute.
	 */
	public void moveLeft() {
		if (!head.isStunned() && head.getCircle().getX() - head.getCircle().getRadius() > 0) {
			translate(-stats.speedPx(), 0);
		}
	}
	
	/**
	 * Moves player rightwards according to speed attribute.
	 */
	public void moveRight() {
		if (!head.isStunned() && head.getCircle().getX() + head.getCircle().getRadius() < Field.WIDTH) {
			translate(stats.speedPx(), 0);
		}
	}
	
	/**
	 * Performs a fast horizontal dash to the left.
	 */
	public void dashLeft() {
		if (!head.isStunned() && head.getCircle().getX() - head.getCircle().getRadius() > 0) {
			translate(-stats.speedPx() * 2, 0);
		}
		collideWithEdges();
	}
	
	/**
	 * Performs a fast horizontal dash to the right.
	 */
	public void dashRight() {
		if (!head.isStunned() && head.getCircle().getX() + head.getCircle().getRadius() < Field.WIDTH) {
			translate(stats.speedPx() * 2, 0);
		}
		collideWithEdges();
	}
	
	/**
	 * Applies an upward jump impulse if player is on the ground or atop another surface.
	 */
	public void jump() {
		double groundY = foot.getCircle().getY() + foot.getCircle().getRadius() + (foot.isKicking() ? 25 : 0);
		if (!head.isStunned() && (groundY >= Field.HEIGHT || onTop)) {
			verticalSpeed = -stats.jumpPx(); 
			onTop = false;
		}
	}
	
	/**
	 * Updates vertical position, gravity, and ground landing contact.
	 */
	public void moveVertical() {
		translate(0, verticalSpeed);
		double groundY = foot.getCircle().getY() + foot.getCircle().getRadius() + (foot.isKicking() ? 25 : 0);
		if (groundY < Field.HEIGHT && !onTop) { 
			verticalSpeed += Physics.GRAVITY;
		} else {
			if (groundY > Field.HEIGHT) {
				translate(0, Field.HEIGHT - groundY);
			}
			verticalSpeed = 0;
		}
	}
	
	/**
	 * Initiates the kick animation and foot displacement.
	 */
	public void kick() {
		if (!head.isStunned()) {
			foot.kicking(side);
		}
	}

	/**
	 * Stops the kick animation and returns the foot to idle position.
	 */
	public void stopKick() {
		foot.stopKicking(side);
	}

	/**
	 * Applies a set of intended input actions onto the player.
	 * 
	 * @param actions Set of requested player actions
	 * @param ball Ball reference for shooting actions
	 * @param rival Opponent player reference for power effects
	 */
	public void applyInput(Set<PlayerAction> actions, Ball ball, Player rival) {
		if (actions == null) {
			stopKick();
			return;
		}

		boolean isKickingAction = false;
		for (PlayerAction action : actions) {
			switch (action) {
				case LEFT:
					moveLeft();
					break;
				case RIGHT:
					moveRight();
					break;
				case JUMP:
					jump();
					break;
				case KICK:
					kick();
					isKickingAction = true;
					break;
				case POWER:
					if (ball != null && rival != null) {
						usePower(ball, rival);
					}
					break;
			}
		}

		if (!isKickingAction) {
			stopKick();
		}
	}

	/**
	 * Applies a set of basic input actions without ball or rival interaction.
	 * 
	 * @param actions Set of requested player actions
	 */
	public void applyInput(Set<PlayerAction> actions) {
		applyInput(actions, null, null);
	}
	
	/**
	 * Resolves collision, stacking, and push mechanics between this player and an opponent.
	 * 
	 * @param player Opponent player to collide with
	 */
	public void collideWithPlayer(Player player) {
		if (this.head.getCircle().detectCollisionWithCircle(player.head.getCircle()) ||
		    this.head.getCircle().detectCollisionWithRectangle(player.body.getRectangle()) ||
		    this.body.getRectangle().detectCollisionWithCircle(player.head.getCircle()) ||
		    this.body.getRectangle().detectCollisionWithRectangle(player.body.getRectangle()) ||
		    this.foot.getCircle().detectCollisionWithCircle(player.head.getCircle()) ||
			this.foot.getCircle().detectCollisionWithCircle(player.foot.getCircle()) ||
			this.foot.getCircle().detectCollisionWithRectangle(player.body.getRectangle()) ||
		    this.head.getCircle().detectCollisionWithCircle(player.foot.getCircle())) {
			
			if (this.foot.getCircle().detectCollisionWithCircle(player.head.getCircle()) && 
					this.foot.getCircle().getY() < player.head.getCircle().getY()) {
				verticalSpeed = 0;
				onTop = true;
			} else {
				onTop = false;
			}
			
			double push = (this.head.getCircle().getX() < player.head.getCircle().getX()) 
					? -player.stats.getSpeed() 
					: player.stats.getSpeed();
			translate(push, 0);
			collideWithEdges();
		} else {
			onTop = false;
		}
	}

	/**
	 * Restricts player movement to remain within horizontal pitch boundaries.
	 */
	public void collideWithEdges() {
		if (head.getCircle().getX() - head.getCircle().getRadius() < 0) {
			double overflow = -(head.getCircle().getX() - head.getCircle().getRadius());
			translate(overflow, 0);
		}
		if (head.getCircle().getX() + head.getCircle().getRadius() > Field.WIDTH) {
			double overflow = (head.getCircle().getX() + head.getCircle().getRadius()) - Field.WIDTH;
			translate(-overflow, 0);
		}
	}
	
	/**
	 * Resolves collision against a goal structure, allowing head collision or standing atop crossbars.
	 * 
	 * @param goal Goal post structure
	 */
	public void collideWithGoal(Goal goal) {
		if (this.head.getCircle().detectCollisionWithRectangle(goal.getCrossBar())) {
			verticalSpeed = 0;
		} else if (this.foot.getCircle().detectCollisionWithRectangle(goal.getCrossBar())) {
			verticalSpeed = 0;
			onTop = true;
		} else {
			onTop = false;
		}
	}

	/**
	 * Checks if the player's foot rests directly on top of the soccer ball.
	 * 
	 * @param ball Ball reference
	 */
	public void collideMyFootWithBall(Ball ball){
		if(this.foot.getCircle().getX() >= ball.getCircle().getX() - ball.getCircle().getRadius() &&
		this.foot.getCircle().getX() <= ball.getCircle().getX() + ball.getCircle().getRadius() &&
		this.foot.getCircle().getY() + this.foot.getCircle().getRadius() >= ball.getCircle().getY() - ball.getCircle().getRadius()){
			onTop = true;
		}
	}

	/**
	 * Executes one tick cycle updating powers, stun timers, foot animations, and gravity.
	 */
	public void tick() {
		if (power != null) {
			power.tick();
		}
		if (head != null && head.isStunned()) {
			head.tick();
		}

		if (foot != null) {
			foot.tick(side);
		}
		moveVertical();
	}

	/**
	 * Returns the player's name.
	 * 
	 * @return Player name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the player's name.
	 * 
	 * @param name Player name to assign
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the player's nationality.
	 * 
	 * @return Country representation
	 */
	public Country getCountry() {
		return country;
	}

	/**
	 * Sets the player's nationality.
	 * 
	 * @param country Country to assign
	 */
	public void setCountry(Country country) {
		this.country = country;
	}

	/**
	 * Returns the player's special power.
	 * 
	 * @return Power instance
	 */
	public Power getPower() {
		return power;
	}

	/**
	 * Assigns the player's special power.
	 * 
	 * @param power Power instance to assign
	 */
	public void setPower(Power power) {
		this.power = power;
	}

	/**
	 * Triggers special power activation on the ball and opponent.
	 * 
	 * @param ball Ball in play
	 * @param rival Opponent player
	 */
	public void usePower(Ball ball, Player rival) {
		if (power != null) {
			power.activate(ball, side);
		}
	}

	/**
	 * Returns the accumulated goals or points.
	 * 
	 * @return Points scored
	 */
	public int getPoints() {
		return points;
	}

	/**
	 * Sets the accumulated goals or points.
	 * 
	 * @param points Points to assign
	 */
	public void setPoints(int points) {
		this.points = points;
	}

	/**
	 * Returns the assigned side of the pitch.
	 * 
	 * @return Side enum value
	 */
	public Side getSide() {
		return side;
	}

	/**
	 * Sets the assigned side of the pitch.
	 * 
	 * @param side Side enum value
	 */
	public void setSide(Side side) {
		this.side = side;
	}

	/**
	 * Returns the head limb component.
	 * 
	 * @return Head instance
	 */
	public Head getHead() {
		return head;
	}

	/**
	 * Sets the head limb component.
	 * 
	 * @param head Head instance to assign
	 */
	public void setHead(Head head) {
		this.head = head;
	}

	/**
	 * Returns the body limb component.
	 * 
	 * @return Body instance
	 */
	public Body getBody() {
		return body;
	}

	/**
	 * Sets the body limb component.
	 * 
	 * @param body Body instance to assign
	 */
	public void setBody(Body body) {
		this.body = body;
	}

	/**
	 * Returns the foot limb component.
	 * 
	 * @return Foot instance
	 */
	public Foot getFoot() {
		return foot;
	}

	/**
	 * Sets the foot limb component.
	 * 
	 * @param foot Foot instance to assign
	 */
	public void setFoot(Foot foot) {
		this.foot = foot;
	}

	/**
	 * Returns the player's statistical attributes.
	 * 
	 * @return PlayerStats instance
	 */
	public PlayerStats getStats() {
		return stats;
	}

	/**
	 * Sets the player's statistical attributes.
	 * 
	 * @param stats PlayerStats to assign
	 */
	public void setStats(PlayerStats stats) {
		this.stats = stats;
	}

	/**
	 * Returns the current vertical velocity.
	 * 
	 * @return Vertical speed in pixels per tick
	 */
	public double getVerticalSpeed() {
		return verticalSpeed;
	}

	/**
	 * Sets the vertical velocity.
	 * 
	 * @param verticalSpeed Vertical speed in pixels per tick
	 */
	public void setVerticalSpeed(double verticalSpeed) {
		this.verticalSpeed = verticalSpeed;
	}

	/**
	 * Checks if the player is currently resting on a surface.
	 * 
	 * @return True if supported, false otherwise
	 */
	public boolean isOnTop() {
		return onTop;
	}

	/**
	 * Sets whether the player is supported on top of a surface.
	 * 
	 * @param onTop True if supported, false otherwise
	 */
	public void setOnTop(boolean onTop) {
		this.onTop = onTop;
	}
}
