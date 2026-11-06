package domain;

public class Field {
	
	private Goal leftGoal;
	private Goal rightGoal;
	
	public static final int WIDTH = 350;
	public static final int HEIGHT = 200;

	public Goal getLeftGoal() {
		return leftGoal;
	}

	public void setLeftGoal(Goal leftGoal) {
		this.leftGoal = leftGoal;
	}

	public Goal getRightGoal() {
		return rightGoal;
	}

	public void setRightGoal(Goal rightGoal) {
		this.rightGoal = rightGoal;
	}
}
