package domain;

import java.io.Serializable;

/**
 * Represents the 2D soccer field containing boundaries and goal areas.
 * 
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class Field implements Serializable {
	private static final long serialVersionUID = 1L;
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
	 * Gets the right goal entity.
	 * 
	 * @return Right goal
	 */
	public Goal getRightGoal() {
		return rightGoal;
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
