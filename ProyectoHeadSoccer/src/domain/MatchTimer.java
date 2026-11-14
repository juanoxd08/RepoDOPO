package domain;

/**
 * Match countdown timer managing remaining match time in seconds and fractional frames.
 * Supports configurable duration in minutes or seconds.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class MatchTimer {
	private int durationSeconds;
	private int timeRemaining;
	private int tickCount;

	public MatchTimer() {
		this(60);
	}

	public MatchTimer(int durationSeconds) {
		this.durationSeconds = durationSeconds;
		this.timeRemaining = durationSeconds;
		this.tickCount = 0;
	}

	/**
	 * Updates the timer by one frame tick at 60 FPS.
	 */
	public void tick() {
		if (timeRemaining > 0) {
			tickCount++;
			if (tickCount >= 60) {
				timeRemaining--;
				tickCount = 0;
			}
		}
	}

	public void decrementTime() {
		if (timeRemaining > 0) {
			timeRemaining--;
		}
	}

	public boolean isTimeUp() {
		return timeRemaining <= 0;
	}

	public void reset() {
		this.timeRemaining = this.durationSeconds;
		this.tickCount = 0;
	}

	public int getTimeRemaining() {
		return timeRemaining;
	}

	public void setTimeRemaining(int timeRemaining) {
		this.timeRemaining = timeRemaining;
	}

	public int getDurationSeconds() {
		return durationSeconds;
	}

	public void setDurationSeconds(int durationSeconds) {
		this.durationSeconds = durationSeconds;
		reset();
	}

	public String getFormattedTime() {
		int m = timeRemaining / 60;
		int s = timeRemaining % 60;
		return String.format("%02d:%02d", m, s);
	}
}
