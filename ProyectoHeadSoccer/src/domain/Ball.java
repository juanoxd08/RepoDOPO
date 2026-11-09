package domain;

public class Ball implements Collidable {
	private Circle circle;
	private double verticalSpeed;
	private double horizontalSpeed;
	
	public Ball() {
		this.circle = new Circle(0, 0, 10);
	}

	public Ball(double x, double y, double radius) {
		this.circle = new Circle(x, y, radius);
	}

	public double getVerticalSpeed() {
		return verticalSpeed;
	}
	
	public void setVerticalSpeed(double verticalSpeed) {
		this.verticalSpeed = verticalSpeed;
	}
	
	public double getHorizontalSpeed() {
		return horizontalSpeed;
	}
	
	public void setHorizontalSpeed(double horizontalSpeed) {
		this.horizontalSpeed = horizontalSpeed;
	}
	
	public Circle getCircle() {
		return circle;
	}

	public void setCircle(Circle circle) {
		this.circle = circle;
	}

	@Override
	public void collideWithFoot(Foot foot) {
		double dx = this.circle.getX() - foot.getCircle().getX();
		double dy = this.circle.getY() - foot.getCircle().getY();
		double distance = Math.hypot(dx, dy);
		if (distance == 0) {
			dx = 1;
			dy = -1;
			distance = Math.hypot(dx, dy);
		}
		int powerKick = foot.getPowerKicking();
		setHorizontalSpeed((dx / distance) * powerKick);
		setVerticalSpeed((dy / distance) * powerKick);
	}
	
	public void collideWithGB(Rectangle r) {
		if (circle.getX() >= r.left() && circle.getX() <= r.right()) {
			setVerticalSpeed(-verticalSpeed * Physics.RESTITUTION);
		} else if (circle.getY() >= r.top() && circle.getY() <= r.bottom()) {
			setHorizontalSpeed(-horizontalSpeed * Physics.RESTITUTION);
		} else {
			double cornerX = (circle.getX() < r.left()) ? r.left() : r.right();
			double cornerY = (circle.getY() < r.top()) ? r.top() : r.bottom();
			double dx = circle.getX() - cornerX;
			double dy = circle.getY() - cornerY;
			double distance = Math.hypot(dx, dy);
			if (distance == 0) {
				dx = 0;
				dy = -1;
				distance = 1;
			}
			double speed = Math.hypot(horizontalSpeed, verticalSpeed) * Physics.CORNER_RESTITUTION;
			setHorizontalSpeed((dx / distance) * speed);
			setVerticalSpeed((dy / distance) * speed);
		}
	}
	
	public void collideWithHead(Head head) {
		double dx = this.circle.getX() - head.getCircle().getX();
		double dy = this.circle.getY() - head.getCircle().getY();
		double distance = Math.hypot(dx, dy);
		if (distance == 0) {
			dx = 0;
			dy = -1;
			distance = 1;
		}
		setHorizontalSpeed((dx / distance) * 50);
		setVerticalSpeed((dy / distance) * 50);	
	}
	
	public void collideWithEdges() {
		if (this.circle.getX() - this.circle.getRadius() <= 0) {
			this.circle.setX(this.circle.getRadius());
			setHorizontalSpeed(Math.abs(horizontalSpeed) * Physics.RESTITUTION);
		}
		if (this.circle.getX() + this.circle.getRadius() >= Field.WIDTH) {
			this.circle.setX(Field.WIDTH - this.circle.getRadius());
			setHorizontalSpeed(-Math.abs(horizontalSpeed) * Physics.RESTITUTION);
		}
		if (this.circle.getY() - this.circle.getRadius() <= 0) {
			this.circle.setY(this.circle.getRadius());
			setVerticalSpeed(Math.abs(verticalSpeed) * Physics.RESTITUTION);
		}
		if (this.circle.getY() + this.circle.getRadius() >= Field.HEIGHT) {
			this.circle.setY(Field.HEIGHT - this.circle.getRadius());
			setVerticalSpeed(-Math.abs(verticalSpeed) * Physics.RESTITUTION);
		}
	}
	
	public void tick() {
		circle.translate(horizontalSpeed, verticalSpeed);
		
		if (this.circle.getY() + this.circle.getRadius() < Field.HEIGHT) { 
			verticalSpeed += Physics.GRAVITY;
		} else {
			this.circle.setY(Field.HEIGHT - this.circle.getRadius());
			horizontalSpeed = horizontalSpeed * Physics.FRICTION;
			if (Math.abs(horizontalSpeed) < 0.05) {
				horizontalSpeed = 0;
			}
		}
	}

	public void reset(double x, double y) {
		getCircle().setX(x);
		getCircle().setY(y);
		setHorizontalSpeed(0);
		setVerticalSpeed(0);
	}

	public Ball copy() {
		Ball copy = new Ball(this.circle.getX(), this.circle.getY(), this.circle.getRadius());
		copy.setHorizontalSpeed(horizontalSpeed);
		copy.setVerticalSpeed(verticalSpeed);
		return copy;
	}
}
