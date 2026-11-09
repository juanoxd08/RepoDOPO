package domain;

/**
 * Scoreboard tracking match points, goal scoring, tie status, and match victor.
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
	 * Returns the points of both players in a two-element array [player1, player2].
	 * 
	 * @return Array containing scores
	 */
	public int[] getPoints(){
		return new int[]{player1.getPoints(), player2.getPoints()};
	}
	
	/**
	 * Checks whether the current score is tied.
	 * 
	 * @return True if scores are equal, false otherwise
	 */
	public boolean isTie(){
		return player1.getPoints() == player2.getPoints();
	}
	
	/**
	 * Determines the winning player, or null if the match is tied.
	 * 
	 * @return Winning Player instance, or null if tied
	 */
	public Player getWinner(){
		if(!isTie()) {
			return (player1.getPoints() > player2.getPoints()) ? player1 : player2;
		}
		return null;
	}

	/**
	 * Records a goal into a specific goal net, awarding points to the opponent player.
	 * 
	 * @param goalSide Side of the goal where the ball entered
	 */
	public void golIn(Side goalSide){
		Side sidePlayer = (goalSide == Side.LEFT) ? Side.RIGHT : Side.LEFT;
		if(player1.getSide().equals(sidePlayer)){
			player1.setPoints(getPoints()[0] + 1);
		}else{
			player2.setPoints(getPoints()[1] + 1);
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
