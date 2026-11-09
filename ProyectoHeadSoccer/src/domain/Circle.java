package domain;

public class Circle extends Figure {
	private double radius;

	public Circle() {
		super();
	}

	public Circle(double x, double y, double radius) {
		super(x, y);
		this.radius = radius;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	@Override
	public boolean detectCollisionWithCircle(Circle circle) {
		double dx = this.getX() - circle.getX();
		double dy = this.getY() - circle.getY();
		double radiusSum = this.radius + circle.radius;
		return (dx * dx) + (dy * dy) <= (radiusSum * radiusSum);
	}

	@Override
	public boolean detectCollisionWithRectangle(Rectangle rectangle) {
		return rectangle.detectCollisionWithCircle(this);
	}
}
