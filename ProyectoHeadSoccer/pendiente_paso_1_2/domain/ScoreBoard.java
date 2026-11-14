package domain;

import java.util.EnumMap;
import java.util.Optional;



/**
 * Scoreboard tracking match points, goal scoring, tie status, and match victor.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class ScoreBoard {
    private final EnumMap<Side, Integer> goals;

	/**
	 * Constructs a scoreboard linked to the two participating players.
	 * 
	 * @param player1 First player instance
	 * @param player2 Second player instance
	 */
	public ScoreBoard(){
	  this.goals = new EnumMap<>(Side.class);
	  this.goals.put(Side.LEFT, 0);
	  this.goals.put(Side.RIGHT, 0);
	}
	
	/**
	 * Returns the points of both players in a two-element array [player1, player2].
	 * 
	 * @return Array containing scores
	 */
	public int getGoals(Side side){
		return goals.get(side);
	}
	
	/**
	 * Checks whether the current score is tied.
	 * 
	 * @return True if scores are equal, false otherwise
	 */
	public boolean isTie(){
		return goals.get(Side.LEFT).equals(goals.get(Side.RIGHT));
	}
	


	/**
	 * Records a goal into a specific goal net, awarding points to the opponent player.
	 * 
	 * @param goalSide Side of the goal where the ball entered
	 */
	public void golIn(Side goalSide){
	    int goals_ = goals.get(goalSide.opposite());
	    goals.put(goalSide.opposite(), goals_ + 1);
	}

	/**
	 * Alias for recording a goal into a specific goal net.
	 * 
	 * @param goalSide Side of the goal where the ball entered
	 */
	public void goalIn(Side goalSide) {
		golIn(goalSide);
	}

	public Optional<Side> getLeader() {
        if (isTie()) return Optional.empty();
        return Optional.of(getGoals(Side.LEFT) > getGoals(Side.RIGHT) ? Side.LEFT : Side.RIGHT);
	}
}
