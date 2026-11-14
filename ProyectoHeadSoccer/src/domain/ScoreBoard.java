package domain;

/**
 * Scoreboard tracking match goals, tie status, and match victor.
 * In DOPOSoccer, goals are tracked separately from player progression points.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class ScoreBoard {
	private Player player1;
	private Player player2;

	/**
	 * Constructs a scoreboard linked to the two participating players.
	 * 
	 * @param player1 First player instance
	 * @param player2 Second player instance
	 */
	public ScoreBoard(Player player1, Player player2){
		this.player1 = player1;
		this.player2 = player2;
	}
	
	/**
	 * Returns the goals of both players in a two-element array [player1Goals, player2Goals].
	 * 
	 * @return Array containing scores
	 */
	public int[] getPoints(){
		int g1 = (player1 != null) ? player1.getGoals() : 0;
		int g2 = (player2 != null) ? player2.getGoals() : 0;
		return new int[]{g1, g2};
	}
	
	/**
	 * Checks whether the current score is tied.
	 * 
	 * @return True if scores are equal, false otherwise
	 */
	public boolean isTie(){
		int[] p = getPoints();
		return p[0] == p[1];
	}
	
	/**
	 * Determines the winning player, or null if the match is tied.
	 * 
	 * @return Winning Player instance, or null if tied
	 */
	public Player getWinner(){
		if(!isTie()) {
			int[] p = getPoints();
			return (p[0] > p[1]) ? player1 : player2;
		}
		return null;
	}

	/**
	 * Records a goal into a specific goal net, awarding a goal to the opponent player.
	 * 
	 * @param goalSide Side of the goal where the ball entered
	 */
	public void golIn(Side goalSide){
		Side sidePlayer = (goalSide == Side.LEFT) ? Side.RIGHT : Side.LEFT;
		if(player1.getSide().equals(sidePlayer)){
			player1.addGoal();
		}else{
			player2.addGoal();
		}
	}

	/**
	 * Alias for recording a goal into a specific goal net.
	 * 
	 * @param goalSide Side of the goal where the ball entered
	 */
	public void goalIn(Side goalSide) {
		golIn(goalSide);
	}
}
