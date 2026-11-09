package domain;

public class Head implements Collidable {
	private Circle circle;
	private int kicksRecieved;
	private int kickTicks;
	private boolean isStunned;
	private boolean struck;

	public Head() {
		this.circle = new Circle();
	}

	public Head(double x, double y, double radius) {
		this.circle = new Circle(x, y, radius);
	}

	public Head(double playerX, Side side) {
		double radius = 20;
		double headY = Field.HEIGHT - 55;
		this.circle = new Circle(playerX, headY, radius);
	}

	public Circle getCircle() {
		return circle;
	}

	public void setCircle(Circle circle) {
		this.circle = circle;
	}

	public int getKicksRecieved() {
		return kicksRecieved;
	}

	public void setKicksRecieved(int kicksRecieved) {
		this.kicksRecieved = kicksRecieved;
	}

	public boolean isStunned() {
		return isStunned;
	}

	public void setStunned(boolean isStunned) {
		this.isStunned = isStunned;
	}	
	
	@Override
	public void collideWithFoot(Foot foot) {
		boolean kicking = foot.isKicking();
		if (kicking) {
			if (!struck) {
				struck = true;
				kicksRecieved += 1;
				isStruck(foot);
			}
			if (kicksRecieved >= 5) {
				kicksRecieved = 0;
				kickTicks = (int)(Physics.seconds(3));
				isStunned = true;
			}
		}
	}
	
	private void isStruck(Foot foot) {
		if (this.getCircle().getX() > foot.getCircle().getX()) {
			this.getCircle().translate(8, -10);
		} else {
			this.getCircle().translate(-8, -10);
		}
	}
	
	public void isNotStruck(Foot foot) {
		if (struck) {
			if (this.getCircle().getX() > foot.getCircle().getX()) {
				this.getCircle().translate(-8, 10);
			} else {
				this.getCircle().translate(8, 10);
			}
			struck = false;
		}	
	}

	public void tick(){
		if(isStunned){
			kickTicks--;
			if(kickTicks <= 0){
				isStunned = false;
			}
		}
	}


}
