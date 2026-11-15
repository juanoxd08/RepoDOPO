package domain;

/**
 * Performance statistics governing speed, jump, kick damage multiplier, and maximum health (survival).
 * All stats scale from 1 to 10 according to the DOPOSoccer specification.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class PlayerStats {
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
	 */
	public PlayerStats(int speed, int jump, int kick, int survival) {
		this.speed = validate(speed);
		this.jump = validate(jump);
		this.kick = validate(kick);
		this.survival = validate(survival);
	}

	private int validate(int val) {
		if (val < 1 || val > 10) {
			throw new IllegalArgumentException(DOPOSoccerException.INVALID_STAT + ": " + val);
		}
		return val;
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
		return 12.8 * (1.0 + 0.10 * (jump - 1));
	}

	/**
	 * Computes the kick damage multiplier formula:
	 * multiplicadorKick = 1 + 0.10 * (Kick - 1)
	 * 
	 * @return Damage multiplier (1.0 to 1.9)
	 */
	public double getMultiplicadorKick() {
		return 1.0 + 0.10 * (kick - 1);
	}

	/**
	 * Computes normal kick damage against rival:
	 * danioPatada = 10 * multiplicadorKick
	 * 
	 * @return Kick damage amount
	 */
	public double getDanioPatada() {
		return 10.0 * getMultiplicadorKick();
	}

	/**
	 * Computes maximum player health based on survival rating:
	 * vidaMax = 100 * (1 + 0.10 * (Survival - 1))
	 * 
	 * @return Maximum health points (100 to 190)
	 */
	public double getVidaMax() {
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

	public int getSpeed() { return speed; }
	public void setSpeed(int speed) {
		this.speed = validate(speed);
	}

	public int getJump() { return jump; }
	public void setJump(int jump) {
		this.jump = validate(jump);
	}

	public int getKick() { return kick; }
	public void setKick(int kick) {
		this.kick = validate(kick);
	}

	public int getSurvival() { return survival; }
	public void setSurvival(int survival) {
		this.survival = validate(survival);
	}
}
