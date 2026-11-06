package domain;

public class Circle extends Figure{
	private int radious;

	public int getRadious() {
		return radious;
	}

	public void setRadious(int radious) {
		this.radious = radious;
	}

	@Override
	public boolean detectCollisionWithCircle(Circle circle) {
		int dx = this.x - circle.x;
		int dy = this.y - circle.y;
		int radiousSum = this.radious + circle.radious;
		return (dx * dx) + (dy * dy) <= (radiousSum * radiousSum);
	}

	@Override
	public boolean detectCollisionWithRectangle(Rectangle rectangle) {
		return rectangle.detectCollisionWithCircle(this);
	}
}
