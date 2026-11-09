package domain;

/**
 * Freezing special power that launches the ball with high speed and freezes the rival upon impact.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class FreezePower extends Power {

	/**
	 * Applies the freeze effect by stunning the rival's head.
	 * 
	 * @param ball Soccer ball in play
	 * @param rival Opponent player
	 */
	@Override
	public void applyPower(Ball ball, Player rival) {
		if (rival != null && rival.getHead() != null) {
			rival.getHead().setStunned(true);
		}
	}

	/**
	 * Restores rival state when freeze effect expires.
	 * 
	 * @param rival Opponent player
	 */
	@Override
	public void stopPower(Player rival) {
		if (rival != null && rival.getHead() != null) {
			rival.getHead().setStunned(false);
		}
	}
	
	/**
	 * Propels the ball with high velocity towards the opponent's goal.
	 * 
	 * @param ball Soccer ball in play
	 * @param side Shooting player field side
	 */
	@Override
	public void affectBall(Ball ball, Side side) {
		if (ball == null) return;
		double dir = (side == Side.LEFT) ? 1.0 : -1.0;
		ball.setHorizontalSpeed(18 * dir);
		ball.setVerticalSpeed(-8);
	}
}
