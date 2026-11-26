package domain;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Represents a soccer player entity composed of head, body, and foot limbs, with health, stats, power, and status effects.
 * Complies with DOPOSoccer specifications for damage, powers, health, and stun mechanics.
 * 
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class Player implements PlayerView, Serializable {
	private static final long serialVersionUID = 1L;
	private final PlayerProfile profile;
	private final Country country;
	private final Side side;
	private final Power power;
	private final PlayerBody playerBody;
	private final Life life;
	private final EffectState effect;
	private boolean wasKicking = false;
	private boolean wasPowerPressed = false;
	private boolean kickDealtDamage = false;

	/**
	 * Constructs a match player backed by a persistent player profile, resolving power from the country.
	 *
	 * @param profile Persistent player profile
	 * @param country Country representation
	 * @param side Pitch side assigned
	 */
	public Player(PlayerProfile profile, Country country, Side side) {
		this(profile, country, side, (country != null) ? country.getPower() : null);
	}

	/**
	 * Constructs a match player backed by a persistent player profile.
	 *
	 * @param profile Persistent player profile
	 * @param country Country representation
	 * @param side Pitch side assigned
	 * @param power Special power ability
	 */
	public Player(PlayerProfile profile, Country country, Side side, Power power) {
		this.profile = (profile != null) ? profile : new PlayerProfile("Player");
		this.country = country;
		this.side = side;
		this.power = power;
		this.playerBody = new PlayerBody(side);
		this.playerBody.getFoot().setPowerKicking((int) this.profile.getStats().kickBallForce());
		this.life = new Life(this.profile.getStats().getMaxLife());
		this.effect = new EffectState();
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
		this(new PlayerProfile(name, stats), country, side, power);
	}

	/**
	 * Resets player position and vertical state to kickoff coordinates.
	 */
	public void resetPosition() {
		this.playerBody.resetPosition();
		this.playerBody.getFoot().setPowerKicking((int) profile.getStats().kickBallForce());
		clearEffect();
	}

	/**
	 * Translates all limbs of the player along Cartesian axes.
	 * 
	 * @param dx Horizontal offset in pixels
	 * @param dy Vertical offset in pixels
	 */
	public void translate(double dx, double dy) {
		this.playerBody.translate(dx, dy);
	}

	/**
	 * Indicates if player is currently disabled from moving, jumping or kicking.
	 * 
	 * @return True if disabled
	 */
	public boolean isDisabled() {
		return life.isStunned() || !effect.canMove()
				|| (power != null && power.isChargingShot());
	}

	/**
	 * Moves player horizontally to the left according to speed stat and status effects.
	 */
	public void moveLeft() {
		if (isDisabled()) return;
		if (effect.isConfused()) {
			moveRightInternal();
			return;
		}
		moveLeftInternal();
	}

	/**
	 * Moves player horizontally to the right according to speed stat and status effects.
	 */
	public void moveRight() {
		if (isDisabled()) return;
		if (effect.isConfused()) {
			moveLeftInternal();
			return;
		}
		moveRightInternal();
	}

	private void moveLeftInternal() {
		double spd = profile.getStats().speedPx() * effect.speedFactor();
		playerBody.moveHorizontally(-spd);
		playerBody.collideWithEdges();
	}

	private void moveRightInternal() {
		double spd = profile.getStats().speedPx() * effect.speedFactor();
		playerBody.moveHorizontally(spd);
		playerBody.collideWithEdges();
	}
	
	/**
	 * Applies an upward jump impulse if player is on the ground or atop another surface.
	 */
	public void jump() {
		if (isDisabled() || !effect.canJump()) return;
		playerBody.jump(profile.getStats().jumpPx());
	}
	
	/**
	 * Updates vertical position, gravity, status effect timers, and ground landing contact.
	 */
	public void moveVertical() {
		life.tick();
		effect.tick(playerBody, side);
		playerBody.tick();
	}

	/**
	 * Frame tick updating power charge and physics.
	 */
	public void tick() {
		if (power != null) {
			power.tickCharge();
		}
		moveVertical();
	}
	
	/**
	 * Initiates the kick animation and foot displacement.
	 */
	public void kick() {
		if (isDisabled() || !effect.canKick()) return;
		if (!playerBody.isKicking()) {
			kickDealtDamage = false;
		}
		playerBody.startKick();
	}

	/**
	 * Stops the kick animation and returns the foot to idle position.
	 */
	public void stopKick() {
		playerBody.stopKick();
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
	 * Applies damage to player health, triggering a 3-second stun at zero health.
	 * 
	 * @param damage Damage amount to deduct
	 */
	public void takeDamage(double damage) {
		life.takeDamage(damage);
	}

	/**
	 * Applies a status effect from an opponent power, replacing any active effect.
	 * 
	 * @param statusEffect Status effect to apply
	 * @param durationSeconds Duration in seconds
	 */
	public void applyEffect(PlayerEffect statusEffect, double durationSeconds) {
		Effect next = (statusEffect != null) ? statusEffect.createEffect() : new NoneEffect();
		effect.apply(next, durationSeconds, playerBody);
	}

	/**
	 * Clears active status effect.
	 */
	public void clearEffect() {
		effect.clear();
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
	 * Clears support state before collision resolution on each tick.
	 */
	public void clearSupport() {
		playerBody.clearSupport();
	}

	/**
	 * Resolves collision, stacking, and push mechanics between this player and an opponent.
	 * 
	 * @param player Opponent player to collide with
	 */
	public void collideWithPlayer(Player player) {
		if (player == null) return;
		playerBody.collideWithPlayer(player.playerBody, player.profile.getStats().speedPx());

		if (!kickDealtDamage && !isDisabled() && playerBody.isKickHitting(player.playerBody)) {
			player.takeDamage(profile.getStats().getKickDamage());
			kickDealtDamage = true;
		}
	}

	/**
	 * Restricts player movement to remain within horizontal pitch boundaries.
	 */
	public void collideWithEdges() {
		playerBody.collideWithEdges();
	}
	
	/**
	 * Resolves physical collision with soccer goal crossbar and net structures.
	 * 
	 * @param goal Goal entity to collide against
	 */
	public void collideWithGoal(Goal goal) {
		playerBody.collideWithGoal(goal);
	}

	/**
	 * Resolves ball collisions against this player's body, head, and foot.
	 *
	 * @param ball Ball in play
	 */
	public boolean collideWithBall(Ball ball) {
		boolean hadArua = hasAura();
		boolean collide = playerBody.collideWithBall(ball, power);
		if(hadArua && !hasAura()){
			ball.setPowerShotCountry(country);
		}
		return collide;
	}

	/**
	 * Returns this player's kick damage multiplier from stats.
	 *
	 * @return Kick multiplier
	 */
	public double getKickMultiplier() {
		return profile.getStats().getKickMultiplier();
	}

	/**
	 * Applies damage and status effect from an opponent power hit, as rule 5 of the statement says,
	 * even if this player has an active aura.
	 *
	 * @param hit Power hit payload to apply
	 */
	public void receiveHit(PowerHit hit) {
		if (hit == null) return;
		if (hit.getDamage() > 0) {
			takeDamage(hit.getDamage());
		}
		if (hit.getEffect() != null && hit.getEffect().getType() != PlayerEffect.NONE) {
			effect.apply(hit.getEffect(), hit.getDurationSeconds(), playerBody);
		}
	}

	/**
	 * Checks whether the ball overlaps any part of this player (head, body, or foot).
	 *
	 * @param ball Ball in play
	 * @return True if any limb overlaps the ball
	 */
	public boolean isHitByBall(Ball ball) {
		return playerBody.isHitByBall(ball);
	}

	/**
	 * Ends any active power shot state for this player.
	 */
	public void finishPowerShot() {
		if (power != null) {
			power.finishPowerShot();
		}
	}

	/**
	 * Advances per-frame power shot effects such as illusion clones.
	 *
	 * @param ball Ball in play
	 */
	public void updatePowerShot(Ball ball) {
		if (power != null) {
			power.updateShot(ball);
		}
	}

	/**
	 * Evaluates whether this player's active power shot hits the opponent and returns the hit payload.
	 *
	 * @param rival Opponent player
	 * @param ball Ball in play
	 * @return PowerHit payload if hit, or null otherwise
	 */
	public PowerHit powerHitOn(Player rival, Ball ball) {
		if (power == null || rival == null || !power.isPowerShotActive() || !rival.isHitByBall(ball)) {
			return null;
		}
		return power.onHitRival(getKickMultiplier());
	}

	public String getName() { return profile.getName(); }
	public Country getCountry() { return country; }
	public Side getSide() { return side; }
	public double getVerticalSpeed() { return playerBody.getVerticalSpeed(); }
	public int getPoints() { return profile.getPoints(); }
	public void addPoints(int amount) { profile.addPoints(amount); }
	public int getStunTicksRemaining() { return life.getStunTicksRemaining(); }

	/**
	 * Returns the underlying player profile for domain persistence coordination.
	 *
	 * @return Player profile
	 */
	PlayerProfile getProfile() {
		return profile;
	}

	@Override
	public double getLife() {
		return life.getCurrent();
	}

	@Override
	public double getMaxLife() {
		return life.getMax();
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
		return life.isStunned();
	}

	@Override
	public boolean isKicking() {
		return playerBody.isKicking();
	}

	@Override
	public String getActiveEffectName() {
		return (effect.getCurrent() != PlayerEffect.NONE) ? effect.getCurrent().name() : "";
	}

	@Override
	public PlayerEffect getActiveEffect() {
		return effect.getCurrent();
	}

	@Override
	public double getEffectProgress() {
		return effect.getProgress();
	}

	@Override
	public Circle getHeadShape() {
		return playerBody.getHeadShape();
	}

	@Override
	public Rectangle getBodyShape() {
		return playerBody.getBodyShape();
	}

	@Override
	public Circle getFootShape() {
		return playerBody.getFootShape();
	}

	@Override
	public int getStat(StatType type) {
		return profile.getStat(type);
	}

	/**
	 * Decoy balls currently created by this player's power (Illusion).
	 *
	 * @return Copies of the decoy balls, or an empty list
	 */
	public List<Circle> getDecoyBalls() {
		if (power == null) {
			return Collections.emptyList();
		}
		return power.getDecoyBalls();
	}
}
