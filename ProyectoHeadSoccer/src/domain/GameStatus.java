package domain;

/**
 * Lifecycle states of a DOPOSoccer match.
 * 
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public enum GameStatus {
	NOT_STARTED,
	PLAYING,
	PAUSED,
	GOAL_SCORED,
	SUDDEN_DEATH,
	FINISHED
}
