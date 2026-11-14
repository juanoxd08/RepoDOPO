package domain;

/**
 * Represents the 2D soccer field containing boundaries and goal areas.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class Field {
	public static final int WIDTH = 350;
	public static final int HEIGHT = 200;

	private Goal leftGoal;
	private Goal rightGoal;

	/**
	 * Default constructor initializing field with left and right goals.
	 */
	public Field() {
		this.leftGoal = new Goal(Side.LEFT);
		this.rightGoal = new Goal(Side.RIGHT);
	}

	/**
	 * Gets the left goal entity.
	 * 
	 * @return Left goal
	 */
	public Goal getLeftGoal() {
		return leftGoal;
	}

	/**
	 * Sets the left goal entity.
	 * 
	 * @param leftGoal New left goal
	 */
	public void setLeftGoal(Goal leftGoal) {
		this.leftGoal = leftGoal;
	}

	/**
	 * Gets the right goal entity.
	 * 
	 * @return Right goal
	 */
	public Goal getRightGoal() {
		return rightGoal;
	}

	/**
	 * Sets the right goal entity.
	 * 
	 * @param rightGoal New right goal
	 */
	public void setRightGoal(Goal rightGoal) {
		this.rightGoal = rightGoal;
	}

	/**
	 * Gets the goal associated with a specific field side.
	 * 
	 * @param side Side of the field
	 * @return Corresponding goal entity
	 */
	public Goal getGoal(Side side) {
		return (side == Side.LEFT) ? leftGoal : rightGoal;
	}
}
