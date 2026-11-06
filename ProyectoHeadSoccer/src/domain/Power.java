package domain;

public abstract class Power {
	protected int charge;
	protected boolean active;
	
	public abstract void applyPower(Ball ball, Player rival);
	public abstract void stopPower(Player rival);
	public abstract void affectBall(Ball ball, Side side);
	
}
