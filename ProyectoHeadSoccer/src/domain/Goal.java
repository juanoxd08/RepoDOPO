package domain;

/**
 * Represents a soccer goal consisting of a physical crossbar and an interior goal area.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class Goal {
	private Rectangle crossBar;
	private Rectangle goalArea;
	private Side side;

	/**
	 * Constructs a goal for specified side of the field.
	 * 
	 * @param side Side of the field (LEFT or RIGHT)
	 */
	public Goal(Side side) {
		this.side = side;
		double initialX = (side == Side.LEFT) ? 0 : Field.WIDTH - 60;
		this.crossBar = new Rectangle(initialX, (double)(Field.HEIGHT - 100), 60.0, 15.0);
		this.goalArea = new Rectangle(initialX, (double)(Field.HEIGHT - 85), 60.0, 85.0);
	}

	/**
	 * Gets the physical crossbar rectangle.
	 * 
	 * @return Crossbar rectangle
	 */
	public Rectangle getCrossBar() {
		return crossBar;
	}

	/**
	 * Gets the interior goal area rectangle.
	 * 
	 * @return Goal area rectangle
	 */
	public Rectangle getGoalArea() {
		return goalArea;
	}

	/**
	 * Gets the rectangle representing the goal area.
	 * 
	 * @return Rectangle geometry of goal area
	 */
	public Rectangle getRectangle() {
		return goalArea;
	}

	/**
	 * Gets the field side of the goal.
	 * 
	 * @return Field side
	 */
	public Side getSide() {
		return side;
	}

	/**
	 * Detects whether the ball has fully entered the goal area.
	 * 
	 * @param ball Ball in play
	 * @return True if a goal is scored, false otherwise
	 */
	public boolean detectGoal(Ball ball) {
		return goalArea.containsCircle(ball.getCircle());
	}
}
