package domain;

public abstract class Figure {
	protected int x;
	protected int y;
	
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	
	public abstract boolean detectCollisionWithCircle(Circle circle);
	public abstract boolean detectCollisionWithRectangle(Rectangle rectangle);
	
	
	
	
	
}
