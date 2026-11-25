package domain;

import java.io.Serializable;

/**
 * Performance statistics governing speed, jump, kick damage multiplier, and maximum health (survival).
 * All stats scale from 1 to 10 according to the DOPOSoccer specification.
 * 
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class PlayerStats implements Serializable {
	private static final long serialVersionUID = 1L;
	private int speed;
	private int jump;
	private int kick;
	private int survival;

	/**
	 * Default constructor initializing all statistics at baseline rating 1.
	 */
	public PlayerStats() {
		this.speed = 1;
		this.jump = 1;
		this.kick = 1;
		this.survival = 1;
	}

	/**
	 * Constructs player statistics with explicit ratings.
	 *
	 * @param speed Movement speed rating (1 to 10)
	 * @param jump Jump rating (1 to 10)
	 * @param kick Kick damage rating (1 to 10)
	 * @param survival Health rating (1 to 10)
	 * @throws DOPOSoccerException if any stat rating is outside [1, 10]
	 */
	public PlayerStats(int speed, int jump, int kick, int survival) throws DOPOSoccerException {
		this.speed = validate(speed);
		this.jump = validate(jump);
		this.kick = validate(kick);
		this.survival = validate(survival);
	}

	/*
	 * Validates that a stat level falls within [1, 10].
	 */
	private int validate(int val) throws DOPOSoccerException {
		if (val < 1 || val > 10) {
			throw new DOPOSoccerException(DOPOSoccerException.INVALID_STAT);
		}
		return val;
	}

	/**
	 * Increments the specified statistic by one level up to the maximum rating of 10.
	 *
	 * @param statType Statistic to increment
	 * @throws DOPOSoccerException if statType is null or already at maximum level
	 */
	public void increment(StatType statType) throws DOPOSoccerException{
		if (statType == null) {
			throw new DOPOSoccerException(DOPOSoccerException.NULL_ARGUMENT);
		}
		switch (statType) {
			case SPEED:
        		if (speed >= 10) throw new DOPOSoccerException(DOPOSoccerException.MAX_STAT_REACHED);
        		speed++;
				break;

			case JUMP:
        		if (jump >= 10) throw new DOPOSoccerException(DOPOSoccerException.MAX_STAT_REACHED);
        		jump++;
				break;
				
			case KICK:
        		if (kick >= 10) throw new DOPOSoccerException(DOPOSoccerException.MAX_STAT_REACHED);
        		kick++;
				break;

			case SURVIVAL:
        		if (survival >= 10) throw new DOPOSoccerException(DOPOSoccerException.MAX_STAT_REACHED);
        		survival++;
				break;
			default:
				break;
		} 
	}

	/**
	 * Returns the displacement in pixels per frame based on speed rating (+10% per level above 1).
	 * 
	 * @return Speed displacement in pixels
	 */
	public double speedPx() {
		return 3.5 * (1.0 + 0.10 * (speed - 1));
	}

	/**
	 * Returns the vertical jump impulse based on jump rating (+10% per level above 1).
	 * 
	 * @return Jump impulse in pixels/tick
	 */
	public double jumpPx() {
		return Physics.BASE_JUMP_IMPULSE * (1.0 + 0.10 * (jump - 1));
	}

	/**
	 * Computes the kick damage multiplier formula:
	 * kickMultiplier = 1 + 0.10 * (kick - 1).
	 *
	 * @return Damage multiplier (1.0 to 1.9)
	 */
	public double getKickMultiplier() {
		return 1.0 + 0.10 * (kick - 1);
	}

	/**
	 * Computes normal kick damage against rival:
	 * kickDamage = 10 * kickMultiplier.
	 *
	 * @return Kick damage amount
	 */
	public double getKickDamage() {
		return 10.0 * getKickMultiplier();
	}

	/**
	 * Computes maximum player health based on survival rating:
	 * maxLife = 100 * (1 + 0.10 * (survival - 1)).
	 *
	 * @return Maximum health points (100 to 190)
	 */
	public double getMaxLife() {
		return 100.0 * (1.0 + 0.10 * (survival - 1));
	}

	/**
	 * Returns the ball impulse force imparted by a kick (unaffected by kick damage rating).
	 * 
	 * @return Kick impulse force for the ball
	 */
	public double kickBallForce() {
		return 18.0;
	}

	/**
	 * Returns the speed stat rating.
	 *
	 * @return Speed rating (1 to 10)
	 */
	public int getSpeed() { return speed; }

	/**
	 * Returns the jump stat rating.
	 *
	 * @return Jump rating (1 to 10)
	 */
	public int getJump() { return jump; }

	/**
	 * Returns the kick stat rating.
	 *
	 * @return Kick rating (1 to 10)
	 */
	public int getKick() { return kick; }

	/**
	 * Returns the survival stat rating.
	 *
	 * @return Survival rating (1 to 10)
	 */
	public int getSurvival() { return survival; }
}
