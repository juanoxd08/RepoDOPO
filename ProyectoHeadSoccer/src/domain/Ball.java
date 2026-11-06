package domain;

public class Ball implements Collidable {
	private Circle circle;
	private double verticalSpeed;
	private double horizontalSpeed;
	public static final int GRAVITY = 2;
	public static final double FRICCION = 0.6;
	
	
	public double getVerticalSpeed() {
		return verticalSpeed;
	}
	
	public void setVerticalSpeed(double verticalSpeed) {
		this.verticalSpeed = verticalSpeed;
	}
	
	public double getHorizontalSpeed() {
		return horizontalSpeed;
	}
	
	public void setHorizontalSpeed(double horizontalSpeed) {
		this.horizontalSpeed = horizontalSpeed;
	}
	
	
	
	public Circle getCircle() {
		return circle;
	}



	@Override
	public void collideWithFoot(Foot foot) {
		double dx = this.circle.x - foot.getCircle().x;
		double dy = this.circle.y - foot.getCircle().y;
		double distance = Math.hypot(dx, dy);
		int powerKick = foot.getPowerKicking();
		setHorizontalSpeed((dx / distance) * powerKick);
		setVerticalSpeed((dy / distance) * powerKick);
	}
	
	public void collideWithGB(Rectangle r) {
		if(circle.x >= r.x && circle.x <= r.x + r.getWidth()) {
			setVerticalSpeed(-verticalSpeed * 0.78);
		} else if(circle.y >= r.y && circle.y <= r.y + r.getHeight()) {
			setHorizontalSpeed(-horizontalSpeed * 0.78);
		} else {
			int cornerX = (circle.x < r.x) ? r.x : r.x + r.getWidth();
			int cornerY = (circle.y < r.y) ? r.y : r.y + r.getHeight();
			double dx = circle.x - cornerX;
			double dy = circle.y - cornerY;
			double distance = Math.hypot(dx, dy);
			double speed = Math.hypot(horizontalSpeed, verticalSpeed) * 0.94;
			setHorizontalSpeed((dx / distance) * speed);
			setVerticalSpeed((dy / distance) * speed);
		}
	}
	
	public void collideWithHead(Head head) {
		double dx = this.circle.x - head.getCircle().x;
		double dy = this.circle.y - head.getCircle().y;
		double distance = Math.hypot(dx, dy);
		setHorizontalSpeed((dx / distance) * 50);
		setVerticalSpeed((dy / distance) * 50);	
	}
	
	public void collideWithEdges(){
		if(this.circle.x - this.circle.getRadious() <= 0) setHorizontalSpeed(-horizontalSpeed * 0.78);
		if(this.circle.x + this.circle.getRadious() >= Field.WIDTH) setHorizontalSpeed(-horizontalSpeed * 0.78);
		if(this.circle.y - this.circle.getRadious() <= 0) setVerticalSpeed(-verticalSpeed * 0.78);
		if(this.circle.y + this.circle.getRadious() >= Field.HEIGHT) setVerticalSpeed(-verticalSpeed * 0.78);
	}
	
	public void move(){
		circle.setY(circle.y + (int)verticalSpeed);
		circle.setX(circle.x + (int)horizontalSpeed);
		
		if(this.circle.y + this.circle.getRadious() < Field.HEIGHT) { 
			verticalSpeed+=GRAVITY;
		}else {
			horizontalSpeed = (int)horizontalSpeed*FRICCION;
		}
		
		
		
		
	}
	

}
