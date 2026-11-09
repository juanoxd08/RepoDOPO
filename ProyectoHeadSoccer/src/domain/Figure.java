package domain;

public abstract class Figure {
	private double x;
	private double y;
	
	public Figure() {
	}

	public Figure(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double getX() {
		return x;
	}
	
	public void setX(double x) {
		this.x = x;
	}
	
	public double getY() {
		return y;
	}
	
	public void setY(double y) {
		this.y = y;
	}
	
	public void translate(double dx, double dy) {
		this.x += dx;
		this.y += dy;
	}
	
	public abstract boolean detectCollisionWithCircle(Circle circle);
	public abstract boolean detectCollisionWithRectangle(Rectangle rectangle);
}
