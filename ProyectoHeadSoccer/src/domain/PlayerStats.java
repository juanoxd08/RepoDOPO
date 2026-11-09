package domain;

/**
 * Performance statistics governing jump, kick strength, and movement speed for a player.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class PlayerStats {
	private int jump;
	private int kick;
	private int speed;

	/**
	 * Constructs player statistics with explicit jump, kick, and speed ratings.
	 * 
	 * @param jump Jump attribute rating (1 to 5)
	 * @param kick Kick attribute rating (1 to 5)
	 * @param speed Speed attribute rating (1 to 5)
	 */
	public PlayerStats(int jump, int kick, int speed) {
		this.jump = jump;
		this.kick = kick;
		this.speed = speed;
	}

	/**
	 * Default constructor initializing balanced baseline statistics.
	 */
	public PlayerStats() {
		this.jump = 3;
		this.kick = 3;
		this.speed = 3;
	}

	/**
	 * Returns the jump rating.
	 * 
	 * @return Jump level
	 */
	public int getJump() {
		return jump;
	}

	/**
	 * Sets the jump rating ensuring bounds between 1 and 5.
	 * 
	 * @param jump Jump level to set
	 * @throws HeadSoccerException If rating is out of bounds
	 */
	public void setJump(int jump) throws HeadSoccerException {
		if (jump < 1 || jump > 5) {
			throw new HeadSoccerException(HeadSoccerException.PLAYER_STATS_OUT_OF_BOUNDS);
		}
		this.jump = jump;	
	}

	/**
	 * Returns the kick rating.
	 * 
	 * @return Kick level
	 */
	public int getKick() {
		return kick;
	}

	/**
	 * Sets the kick rating ensuring bounds between 1 and 5.
	 * 
	 * @param kick Kick level to set
	 * @throws HeadSoccerException If rating is out of bounds
	 */
	public void setKick(int kick) throws HeadSoccerException {
		if (kick < 1 || kick > 5) {
			throw new HeadSoccerException(HeadSoccerException.PLAYER_STATS_OUT_OF_BOUNDS);
		}
		this.kick = kick;
	}

	/**
	 * Returns the speed rating.
	 * 
	 * @return Speed level
	 */
	public int getSpeed() {
		return speed;
	}

	/**
	 * Sets the speed rating ensuring bounds between 1 and 5.
	 * 
	 * @param speed Speed level to set
	 * @throws HeadSoccerException If rating is out of bounds
	 */
	public void setSpeed(int speed) throws HeadSoccerException {
		if (speed < 1 || speed > 5) {
			throw new HeadSoccerException(HeadSoccerException.PLAYER_STATS_OUT_OF_BOUNDS);
		}
		this.speed = speed;
	}

	/**
	 * Converts the speed rating into horizontal displacement pixels per frame.
	 * 
	 * @return Pixels per frame
	 */
	public double speedPx() {
		return 3.0 + speed * 0.8;
	}

	/**
	 * Converts the jump rating into vertical impulse velocity.
	 * 
	 * @return Vertical impulse velocity
	 */
	public double jumpPx() {
		return 12.0 + jump * 1.5;
	}

	/**
	 * Converts the kick rating into impulse force applied to the ball.
	 * 
	 * @return Kick impulse force
	 */
	public double kickPx() {
		return 15.0 + kick * 2.5;
	}
}
