package domain;

public class Goal {
	private Rectangle rectangle;

	public Rectangle getRectangle() {
		return rectangle;
	}

	public boolean detectGoal(Ball ball) {
		if(this.rectangle.x < ball.getCircle().x && this.rectangle.x + this.rectangle.getWidth() > ball.getCircle().x &&
		   this.rectangle.y + this.rectangle.getHeight() < ball.getCircle().y - ball.getCircle().getRadious() ) return true;
		return false;
	}
}
