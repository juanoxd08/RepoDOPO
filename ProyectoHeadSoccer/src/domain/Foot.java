package domain;

public class Foot implements Collidable {
	private Circle circle;
	private boolean isKicking;
	private int powerKicking;
	private int kickTicks;

	public Foot() {
		this.circle = new Circle();
	}

	public Foot(double x, double y, double radius) {
		this.circle = new Circle(x, y, radius);
	}

	public Foot(double playerX, Side side) {
		// Radio 12, ubicado en el suelo (Field.HEIGHT - 12)
		// Si es LEFT mira hacia la derecha (+8), si es RIGHT hacia la izquierda (-8)
		double radius = 12;
		double footX = (side == Side.LEFT) ? (playerX + 8) : (playerX - 8);
		double footY = Field.HEIGHT - radius;
		this.circle = new Circle(footX, footY, radius);
	}

	public Circle getCircle() {
		return circle;
	}

	public void setCircle(Circle circle) {
		this.circle = circle;
	}
		
	public boolean isKicking() {
		return isKicking;
	}
	
	public int getPowerKicking() {
		if (isKicking) {
			return powerKicking * 5;
		} else {
			return powerKicking;
		}
	}

	public void setPowerKicking(int powerKicking) {
		this.powerKicking = powerKicking;
	}

	public void setKicking(boolean isKicking) {
		this.isKicking = isKicking;
	}
	
	public void kicking(Side side) {
		if (!isKicking) {
			setKicking(true);
			kickTicks = (int)(Physics.seconds(0.12));
			double dx = (side == Side.LEFT) ? 25 : -25;
			circle.translate(dx, -25);
		}
	}

	public void stopKicking(Side side) {
		if (isKicking) {
			setKicking(false);
			kickTicks = 0;
			double dx = (side == Side.LEFT) ? -25 : 25;
			circle.translate(dx, 25);
		}
	}

	public void tick(Side side){
		if (isKicking) {
			kickTicks--;
			if (kickTicks <= 0) {
				stopKicking(side);
			}
		}
	}
}
