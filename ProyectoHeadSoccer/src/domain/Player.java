package domain;

import java.util.Set;

/**
 * Represents a soccer player entity composed of head, body, and foot limbs, with health, stats, power, and status effects.
 * Complies with DOPOSoccer specifications for damage, powers, health, and stun mechanics.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class Player implements Collidable, PlayerView {
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
	private int goals;
	private int points;

	// Health and Status effects
	private double vidaActual;
	private double vidaMax;
	private PlayerEffect currentEffect = PlayerEffect.NONE;
	private int effectTicksRemaining = 0;
	private boolean isStunnedByZeroHealth = false;
	private int stunTicksRemaining = 0;
	private boolean wasKicking = false;
	private boolean wasPowerPressed = false;
	private boolean kickDealtDamage = false;

	/**
	 * Default constructor initializing default player values.
	 */
	public Player() {
		this("Player", Country.SOUTH_KOREA, Side.LEFT, new AuraAzulPower(), new PlayerStats());
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
		double initialX = (side == Side.LEFT) ? 80 : Field.WIDTH - 80;
		this.name = name;
		this.country = country;
		this.side = side;
		this.power = power;
		this.stats = (stats != null) ? stats : new PlayerStats();
		this.verticalSpeed = 0;
		this.onTop = false;
		this.points = 0;

		this.vidaMax = this.stats.getVidaMax();
		this.vidaActual = this.vidaMax;

		this.head = new Head(initialX, side);
		this.body = new Body(initialX, side);
		this.foot = new Foot(initialX, side);
		if (this.stats != null) {
			this.foot.setPowerKicking((int) this.stats.kickBallForce());
		}
	}

	/**
	 * Resets player position and vertical state to kickoff coordinates.
	 */
	public void resetPosition() {
		double initialX = (side == Side.LEFT) ? 80 : Field.WIDTH - 80;
		this.verticalSpeed = 0;
		this.onTop = false;
		this.head = new Head(initialX, side);
		this.body = new Body(initialX, side);
		this.foot = new Foot(initialX, side);
		if (this.stats != null) {
			this.foot.setPowerKicking((int) this.stats.kickBallForce());
		}
		clearEffect();
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
	 * Indicates if player is currently disabled from moving, jumping or kicking.
	 * 
	 * @return True if disabled
	 */
	public boolean isDisabled() {
		return isStunnedByZeroHealth || 
		       currentEffect == PlayerEffect.FROZEN || 
		       currentEffect == PlayerEffect.KNOCKED_OUT ||
		       (head != null && head.isStunned());
	}

	/**
	 * Moves player leftwards according to speed attribute and active effects.
	 */
	public void moveLeft() {
		if (isDisabled()) return;

		// Confusión invierte controles
		if (currentEffect == PlayerEffect.CONFUSED) {
			moveRightInternal();
			return;
		}
		moveLeftInternal();
	}
	
	/**
	 * Moves player rightwards according to speed attribute and active effects.
	 */
	public void moveRight() {
		if (isDisabled()) return;

		// Confusión invierte controles
		if (currentEffect == PlayerEffect.CONFUSED) {
			moveLeftInternal();
			return;
		}
		moveRightInternal();
	}

	private void moveLeftInternal() {
		double spd = stats.speedPx();
		if (currentEffect == PlayerEffect.ELECTROCUTED) spd *= 0.5; // Camerún: 50% velocidad

		if (head.getCircle().getX() - head.getCircle().getRadius() > 0) {
			translate(-spd, 0);
		}
	}

	private void moveRightInternal() {
		double spd = stats.speedPx();
		if (currentEffect == PlayerEffect.ELECTROCUTED) spd *= 0.5; // Camerún: 50% velocidad

		if (head.getCircle().getX() + head.getCircle().getRadius() < Field.WIDTH) {
			translate(spd, 0);
		}
	}
	
	/**
	 * Applies an upward jump impulse if player is on the ground or atop another surface.
	 * Electrocutado (Camerún) impide saltar.
	 */
	public void jump() {
		if (isDisabled()) return;
		if (currentEffect == PlayerEffect.ELECTROCUTED) return; // Camerún: no puede saltar

		double groundY = foot.getCircle().getY() + foot.getCircle().getRadius() + (foot.isKicking() ? 25 : 0);
		if (groundY >= Field.HEIGHT - 2 || onTop) {
			verticalSpeed = -stats.jumpPx(); 
			onTop = false;
		}
	}
	
	/**
	 * Updates vertical position, gravity, status effect timers, and ground landing contact.
	 */
	public void moveVertical() {
		// Manejo de aturdimiento por vida en 0 (3 segundos)
		if (isStunnedByZeroHealth) {
			stunTicksRemaining--;
			if (stunTicksRemaining <= 0) {
				isStunnedByZeroHealth = false;
				vidaActual = vidaMax; // Al terminar el aturdimiento, la vida se reinicia a su vida máxima
				if (head != null) head.setStunned(false);
			}
		}

		// Manejo de efectos temporales de poder
		if (currentEffect != PlayerEffect.NONE && effectTicksRemaining > 0) {
			effectTicksRemaining--;
			if (currentEffect == PlayerEffect.PUSHED) {
				// Empuje hacia su propio arco (Corea del Sur)
				double pushDir = (side == Side.LEFT) ? -1.0 : 1.0;
				translate(pushDir * 4.0, 0);
				collideWithEdges();
			}

			if (effectTicksRemaining <= 0) {
				clearEffect();
			}
		}

		// Física vertical estándar
		translate(0, verticalSpeed);
		if (head != null && head.getCircle() != null) {
			double topY = head.getCircle().getY() - head.getCircle().getRadius();
			if (topY < 0) {
				translate(0, -topY);
				if (verticalSpeed < 0) verticalSpeed = 0;
			}
		}
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
	 * Frame tick updating power charge and physics.
	 */
	public void tick() {
		if (power != null) {
			power.tickCharge();
		}
		if (foot != null) {
			foot.tick(side);
		}
		moveVertical();
	}
	
	/**
	 * Initiates the kick animation and foot displacement.
	 */
	public void kick() {
		if (isDisabled()) return;
		if (!foot.isKicking()) {
			kickDealtDamage = false;
		}
		foot.kicking(side);
	}

	/**
	 * Stops the kick animation and returns the foot to idle position.
	 */
	public void stopKick() {
		foot.stopKicking(side);
	}

	/**
	 * Activates the player's special power aura when power gauge is full.
	 */
	public void activatePower() {
		if (power != null && !isDisabled()) {
			power.activateAura();
		}
	}

	/**
	 * Receives goal notification, adding 25% to the power gauge according to rules.
	 */
	public void onGoalReceived() {
		if (power != null) {
			power.addQuarterOnGoalReceived();
		}
	}

	/**
	 * Applies damage to player health:
	 * La vida nunca puede ser inferior a cero. Cuando llega a cero, el jugador queda aturdido 3 segundos.
	 * 
	 * @param damage Damage amount to deduct
	 */
	public void takeDamage(double damage) {
		if (isStunnedByZeroHealth) return;

		this.vidaActual = Math.max(0.0, this.vidaActual - damage);
		if (this.vidaActual <= 0.0) {
			this.isStunnedByZeroHealth = true;
			this.stunTicksRemaining = (int) (3.0 * 60); // 3 segundos a 60 FPS
			if (head != null) head.setStunned(true);
		}
	}

	/**
	 * Applies a status effect from an opponent power:
	 * Los efectos de los tiros de poder no se acumulan: el nuevo efecto reemplaza al anterior.
	 * 
	 * @param effect Status effect to apply
	 * @param durationSeconds Duration in seconds
	 */
	public void applyEffect(PlayerEffect effect, double durationSeconds) {
		this.currentEffect = effect;
		this.effectTicksRemaining = (int) (durationSeconds * 60);
		if (effect == PlayerEffect.FROZEN && head != null) {
			head.setStunned(true);
		} else if (effect == PlayerEffect.KNOCKED_OUT) {
			this.verticalSpeed = -10.0;
			this.onTop = false;
		}
	}

	/**
	 * Clears active status effect.
	 */
	public void clearEffect() {
		this.currentEffect = PlayerEffect.NONE;
		this.effectTicksRemaining = 0;
		if (!isStunnedByZeroHealth && head != null) {
			head.setStunned(false);
		}
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
			wasKicking = false;
			wasPowerPressed = false;
			return;
		}

		boolean kickingNow = actions.contains(PlayerAction.KICK);
		boolean powerNow = actions.contains(PlayerAction.POWER);

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
					if (!wasKicking) {
						kick();
					}
					break;
				case POWER:
					if (!wasPowerPressed) {
						activatePower();
					}
					break;
			}
		}

		wasKicking = kickingNow;
		wasPowerPressed = powerNow;
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
		if (player == null || player.getHead() == null) return;
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
					? -player.stats.speedPx() 
					: player.stats.speedPx();
			translate(push, 0);
			collideWithEdges();
		} else {
			onTop = false;
		}

		// Patada al rival dentro del alcance (1 golpe por patada)
		if (this.foot.isKicking() && !kickDealtDamage && !isDisabled()) {
			if (this.foot.getCircle().detectCollisionWithCircle(player.head.getCircle()) ||
			    this.foot.getCircle().detectCollisionWithRectangle(player.body.getRectangle())) {
				player.takeDamage(this.stats.getDanioPatada());
				kickDealtDamage = true;
			}
		}
	}

	/**
	 * Restricts player movement to remain within horizontal pitch boundaries.
	 */
	public void collideWithEdges() {
		if (head == null || head.getCircle() == null) return;
		double r = head.getCircle().getRadius();
		if (head.getCircle().getX() - r < 0) {
			translate(-(head.getCircle().getX() - r), 0);
		}
		if (head.getCircle().getX() + r > Field.WIDTH) {
			translate(Field.WIDTH - (head.getCircle().getX() + r), 0);
		}
	}
	
	/**
	 * Resolves physical collision with soccer goal crossbar and net structures.
	 * 
	 * @param goal Goal entity to collide against
	 */
	public void collideWithGoal(Goal goal) {
		if (goal == null || goal.getCrossBar() == null) return;
		Rectangle bar = goal.getCrossBar();
		if (this.head.getCircle().detectCollisionWithRectangle(bar)) {
			verticalSpeed = 0;
		} else if (this.foot.getCircle().detectCollisionWithRectangle(bar)) {
			verticalSpeed = 0;
			onTop = true;
		}
	}

	public void collideMyFootWithBall(Ball ball) {
		if (ball == null || foot == null) return;
		double impulse = foot.isKicking() ? foot.getPowerKicking() : 0.0;
		if (ball.bounceOff(foot.getCircle(), Physics.RESTITUTION, impulse)) {
			if (foot.isKicking() && this.power != null && this.power.isAuraActive()) {
				this.power.executePowerShot(ball, this);
			}
		}
	}

	/**
	 * Resolves ball collisions against this player's body, head, and foot.
	 *
	 * @param ball Ball in play
	 */
	public void collideWithBall(Ball ball) {
		if (ball == null) return;
		if (body != null) {
			ball.bounceOff(body.getRectangle(), Physics.RESTITUTION, 0.0);
		}
		if (head != null) {
			ball.bounceOff(head.getCircle(), Physics.RESTITUTION, 20.0);
		}
		collideMyFootWithBall(ball);
	}

	/**
	 * Checks whether the ball overlaps any part of this player (head, body, or foot).
	 *
	 * @param ball Ball in play
	 * @return True if any limb overlaps the ball
	 */
	public boolean isHitByBall(Ball ball) {
		if (ball == null || ball.getCircle() == null) return false;
		Circle ballCircle = ball.getCircle();
		boolean hitHead = head != null && head.getCircle() != null && ballCircle.detectCollisionWithCircle(head.getCircle());
		boolean hitBody = body != null && body.getRectangle() != null && ballCircle.detectCollisionWithRectangle(body.getRectangle());
		boolean hitFoot = foot != null && foot.getCircle() != null && ballCircle.detectCollisionWithCircle(foot.getCircle());
		return hitHead || hitBody || hitFoot;
	}

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public Country getCountry() { return country; }
	public void setCountry(Country country) { this.country = country; }
	public Side getSide() { return side; }
	public void setSide(Side side) { this.side = side; }
	public Head getHead() { return head; }
	public Body getBody() { return body; }
	public Foot getFoot() { return foot; }
	public PlayerStats getStats() { return stats; }
	public Power getPower() { return power; }
	public void setPower(Power power) { this.power = power; }
	public double getVerticalSpeed() { return verticalSpeed; }
	public void setVerticalSpeed(double verticalSpeed) { this.verticalSpeed = verticalSpeed; }
	public int getGoals() { return goals; }
	public void setGoals(int goals) { this.goals = goals; }
	public void addGoal() { this.goals++; }
	public int getPoints() { return points; }
	public void setPoints(int points) { this.points = points; }
	public void addPoints(int amount) { this.points += amount; }
	public double getVidaActual() { return vidaActual; }
	public double getVidaMax() { return vidaMax; }
	public PlayerEffect getCurrentEffect() { return currentEffect; }
	public boolean isStunnedByZeroHealth() { return isStunnedByZeroHealth; }
	public int getStunTicksRemaining() { return stunTicksRemaining; }

	// --- PlayerView contract ---

	@Override
	public double getLife() {
		return vidaActual;
	}

	@Override
	public double getMaxLife() {
		return vidaMax;
	}

	@Override
	public double getPowerProgress() {
		return (power != null) ? power.getCharge() : 0.0;
	}

	@Override
	public boolean hasAura() {
		return power != null && power.isAuraActive();
	}

	@Override
	public boolean isStunned() {
		return isStunnedByZeroHealth;
	}

	@Override
	public boolean isKicking() {
		return foot != null && foot.isKicking();
	}

	@Override
	public String getActiveEffectName() {
		return (currentEffect != null && currentEffect != PlayerEffect.NONE) ? currentEffect.name() : "";
	}

	@Override
	public Circle getHeadShape() {
		if (head == null || head.getCircle() == null) return null;
		Circle c = head.getCircle();
		return new Circle(c.getX(), c.getY(), c.getRadius());
	}

	@Override
	public Rectangle getBodyShape() {
		if (body == null || body.getRectangle() == null) return null;
		Rectangle r = body.getRectangle();
		return new Rectangle(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}

	@Override
	public Circle getFootShape() {
		if (foot == null || foot.getCircle() == null) return null;
		Circle c = foot.getCircle();
		return new Circle(c.getX(), c.getY(), c.getRadius());
	}

	@Override
	public int getStat(StatType type) {
		if (stats == null || type == null) return 1;
		switch (type) {
			case SPEED: return stats.getSpeed();
			case JUMP: return stats.getJump();
			case KICK: return stats.getKick();
			case SURVIVAL: return stats.getSurvival();
			default: return 1;
		}
	}
}
