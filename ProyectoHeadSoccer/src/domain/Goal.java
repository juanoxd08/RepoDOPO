package domain;

public class Goal {
	private Rectangle crossBar;
	private Rectangle goalArea;
	private Side side;

	public Goal(Side side) {
		this.side = side;
		double initialX = (side == Side.LEFT) ? 0 : Field.WIDTH - 60;
		this.crossBar = new Rectangle(initialX, (double)(Field.HEIGHT - 100), 60.0, 15.0);
		this.goalArea = new Rectangle(initialX, (double)(Field.HEIGHT - 85), 60.0, 85.0);
	}

	public Rectangle getCrossBar() {
		return crossBar;
	}

	public Rectangle getGoalArea() {
		return goalArea;
	}

	public Rectangle getRectangle() {
		return goalArea;
	}

	public Side getSide() {
		return side;
	}

	public boolean detectGoal(Ball ball) {
		return goalArea.containsCircle(ball.getCircle());
	}
}
