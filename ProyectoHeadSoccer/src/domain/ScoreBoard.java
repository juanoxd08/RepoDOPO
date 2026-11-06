package domain;

public class ScoreBoard {
	private Player player1;
	private Player player2;
	
	public int[] getPoints(){
		return new int[]{player1.getPoints(), player2.getPoints()};
	}
	
	public boolean isTie(){
		return player1.getPoints() == player2.getPoints();
	}
	
	public Player getWinner(){
		if(!isTie()) {
			return (player1.getPoints() > player2.getPoints()) ? player1 : player2;
		}
		return null;
	}
	
	
}
