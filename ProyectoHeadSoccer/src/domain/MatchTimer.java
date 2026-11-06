package domain;

public class MatchTimer {
	public static final int INITIAL_TIME = 60;
	private int timeRemaining;

	public MatchTimer() {
		this.timeRemaining = INITIAL_TIME;
	}

	public void decrementTime() {
		if (timeRemaining > 0) {
			timeRemaining -= 1;
		}
	}

	public boolean isTimeUp() {
		return timeRemaining <= 0;
	}

	public int getTimeRemaining() {
		return timeRemaining;
	}

	public void setTimeRemaining(int timeRemaining) {
		this.timeRemaining = timeRemaining;
	}
}
