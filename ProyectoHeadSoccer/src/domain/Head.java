package domain;

public class Head implements Collidable {
	private Circle circle;
	private int kicksRecieved;
	private boolean isStunned;
	private boolean struck;

	public Circle getCircle() {
		return circle;
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
		if(kicking) {
			if(!struck) {
				struck = true;
				kicksRecieved+=1;
				isStruck(foot);
			}
			if(kicksRecieved >= 5){
				kicksRecieved = 0;
				isStunned = true;
			}
			
		}
	}
	
	
	
	private void isStruck(Foot foot) {
		if(this.getCircle().x > foot.getCircle().x) {
			this.getCircle().x+=8;
			this.getCircle().y-=10;
		}else {
			this.getCircle().x-=8;
			this.getCircle().y-=10;			
		}
	}
	
	public void isNotStruck(Foot foot) {
		if(struck) {
			if(this.getCircle().x > foot.getCircle().x) {
				this.getCircle().x-=8;
				this.getCircle().y+=10;
			}else {
				this.getCircle().x+=8;
				this.getCircle().y+=10;			
			}
			struck = false;
		}	
	}
	
	
}
