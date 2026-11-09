package domain;

public class Rectangle extends Figure {
	private double width;
	private double height;

	public Rectangle() {
		super();
	}

	public Rectangle(double x, double y, double width, double height) {
		super(x, y);
		this.width = width;
		this.height = height;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public double left() {
		return getX();
	}

	public double right() {
		return getX() + width;
	}

	public double top() {
		return getY();
	}

	public double bottom() {
		return getY() + height;
	}

	@Override
	public boolean detectCollisionWithCircle(Circle circle) {
		double closestX = Math.max(left(), Math.min(circle.getX(), right()));
		double closestY = Math.max(top(), Math.min(circle.getY(), bottom()));
		double dx = circle.getX() - closestX;
		double dy = circle.getY() - closestY;
		return (dx * dx) + (dy * dy) <= (circle.getRadius() * circle.getRadius());
	}

	@Override
	public boolean detectCollisionWithRectangle(Rectangle rectangle) {
		return rectangle.right() >= this.left()
				&& rectangle.left() <= this.right()
				&& rectangle.bottom() >= this.top()
				&& rectangle.top() <= this.bottom();
	}

	public boolean containsCircle(Circle circle) {
		return circle.getX() - circle.getRadius() >= left()
				&& circle.getX() + circle.getRadius() <= right()
				&& circle.getY() - circle.getRadius() >= top()
				&& circle.getY() + circle.getRadius() <= bottom();
	}
}
