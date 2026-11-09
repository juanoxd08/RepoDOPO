package domain;

public abstract class Power {
	protected int charge;
	protected boolean active;

	public int getCharge() {
		return charge;
	}

	public void setCharge(int charge) {
		this.charge = charge;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isReady() {
		return charge >= 100;
	}
	
	public abstract void applyPower(Ball ball, Player rival);
	public abstract void stopPower(Player rival);
	public abstract void affectBall(Ball ball, Side side);
}
