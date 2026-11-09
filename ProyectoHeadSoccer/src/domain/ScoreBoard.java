package domain;

public class ScoreBoard {
	private Player player1;
	private Player player2;

	public ScoreBoard(Player player1, Player player2){
		this.player1 = player1;
		this.player2 = player2;
	}
	
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

	public void golIn(Side goalSide){
		Side sidePlayer = (goalSide == Side.LEFT) ? Side.RIGHT : Side.LEFT;
		if(player1.getSide().equals(sidePlayer)){
			player1.setPoints(getPoints()[0] + 1);
		}else{
			player2.setPoints(getPoints()[1] + 1);
		}
	}

	public void goalIn(Side goalSide) {
		golIn(goalSide);
	}
	
	
}
