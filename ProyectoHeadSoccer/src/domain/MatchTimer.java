package domain;

/**
 * Match countdown timer managing remaining match time and expiration checks.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class MatchTimer {
	public static final int INITIAL_TIME = 60;
	private int timeRemaining;

	/**
	 * Constructs a match timer initialized to default match duration.
	 */
	public MatchTimer() {
		this.timeRemaining = INITIAL_TIME;
	}

	/**
	 * Decrements remaining time by one second if time is still remaining.
	 */
	public void decrementTime() {
		if (timeRemaining > 0) {
			timeRemaining -= 1;
		}
	}

	/**
	 * Checks whether match time has expired.
	 * 
	 * @return True if time is zero or negative, false otherwise
	 */
	public boolean isTimeUp() {
		return timeRemaining <= 0;
	}

	/**
	 * Returns the remaining time in seconds.
	 * 
	 * @return Seconds remaining
	 */
	public int getTimeRemaining() {
		return timeRemaining;
	}

	/**
	 * Sets the remaining time in seconds.
	 * 
	 * @param timeRemaining Seconds to set
	 */
	public void setTimeRemaining(int timeRemaining) {
		this.timeRemaining = timeRemaining;
	}
}
