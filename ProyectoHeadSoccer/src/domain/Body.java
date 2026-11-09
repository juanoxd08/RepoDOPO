package domain;

public class Body implements Collidable {
	private Rectangle rectangle;

	public Body() {
		this.rectangle = new Rectangle();
	}

	public Body(double x, double y, double width, double height) {
		this.rectangle = new Rectangle(x, y, width, height);
	}

	public Body(double playerX, Side side) {
		// Ancho 26, Alto 22, centrado respecto a playerX
		double width = 26;
		double height = 22;
		double bodyX = playerX - (width / 2.0);
		double bodyY = Field.HEIGHT - 38;
		this.rectangle = new Rectangle(bodyX, bodyY, width, height);
	}

	public Rectangle getRectangle() {
		return rectangle;
	}

	public void setRectangle(Rectangle rectangle) {
		this.rectangle = rectangle;
	}
}
