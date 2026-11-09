package domain;

import java.util.Set;

public class Player implements Collidable {
	private String name;
	private Country country;
	private Side side;
	private Power power; 
	private Head head;
	private Body body;
	private Foot foot;
	private PlayerStats stats;
	private double verticalSpeed;
	private boolean onTop;
	private int points;
	
	public Player() {
		this.verticalSpeed = 0;
		this.onTop = false;
		this.points = 0;
	}

	public Player(String name, Country country, Side side, Power power, PlayerStats stats) {
		double initialX = (side == Side.LEFT) ? 100 : Field.WIDTH - 100;
		this.name = name;
		this.country = country;
		this.side = side;
		this.power = power;
		this.stats = stats;
		this.verticalSpeed = 0;
		this.onTop = false;
		this.points = 0;
		this.head = new Head(initialX, side);
		this.body = new Body(initialX, side);
		this.foot = new Foot(initialX, side);
	}

	public void resetPosition() {
		double initialX = (side == Side.LEFT) ? 100 : Field.WIDTH - 100;
		this.verticalSpeed = 0;
		this.onTop = false;
		this.head = new Head(initialX, side);
		this.body = new Body(initialX, side);
		this.foot = new Foot(initialX, side);
	}

	public void translate(double dx, double dy) {
		if (head != null && head.getCircle() != null) {
			head.getCircle().translate(dx, dy);
		}
		if (body != null && body.getRectangle() != null) {
			body.getRectangle().translate(dx, dy);
		}
		if (foot != null && foot.getCircle() != null) {
			foot.getCircle().translate(dx, dy);
		}
	}

	public void moveLeft() {
		if (!head.isStunned() && head.getCircle().getX() - head.getCircle().getRadius() > 0) {
			translate(-stats.getSpeed(), 0);
		}
	}
	
	public void moveRight() {
		if (!head.isStunned() && head.getCircle().getX() + head.getCircle().getRadius() < Field.WIDTH) {
			translate(stats.getSpeed(), 0);
		}
	}
	
	public void dashLeft() {
		if (!head.isStunned() && head.getCircle().getX() - head.getCircle().getRadius() > 0) {
			translate(-stats.getSpeed() * 2, 0);
		}
		collideWithEdges();
	}
	
	public void dashRight() {
		if (!head.isStunned() && head.getCircle().getX() + head.getCircle().getRadius() < Field.WIDTH) {
			translate(stats.getSpeed() * 2, 0);
		}
		collideWithEdges();
	}
	
	public void jump() {
	    if (!head.isStunned() && (foot.getCircle().getY() + foot.getCircle().getRadius() >= Field.HEIGHT || onTop)) {
	        verticalSpeed = -stats.getJump(); 
	        onTop = false;
	    }
	}
	
	public void moveVertical() {
		translate(0, verticalSpeed);
		if (this.foot.getCircle().getY() + this.foot.getCircle().getRadius() < Field.HEIGHT && !onTop) { 
			verticalSpeed += Physics.GRAVITY;
		} else {
			verticalSpeed = 0;
		}
	}
	
	public void kick() {
		if (!head.isStunned()) {
			foot.kicking(side);
		}
	}

	public void stopKick() {
		foot.stopKicking(side);
	}

	public void applyInput(Set<PlayerAction> actions, Ball ball, Player rival) {
		if (actions == null) {
			stopKick();
			return;
		}

		boolean isKickingAction = false;
		for (PlayerAction action : actions) {
			switch (action) {
				case LEFT:
					moveLeft();
					break;
				case RIGHT:
					moveRight();
					break;
				case JUMP:
					jump();
					break;
				case KICK:
					kick();
					isKickingAction = true;
					break;
				case POWER:
					if (ball != null && rival != null) {
						usePower(ball, rival);
					}
					break;
			}
		}

		if (!isKickingAction) {
			stopKick();
		}
	}

	public void applyInput(Set<PlayerAction> actions) {
		applyInput(actions, null, null);
	}
	
	public void collideWithPlayer(Player player) {
		if (this.head.getCircle().detectCollisionWithCircle(player.head.getCircle()) ||
		    this.head.getCircle().detectCollisionWithRectangle(player.body.getRectangle()) ||
		    this.body.getRectangle().detectCollisionWithCircle(player.head.getCircle()) ||
		    this.body.getRectangle().detectCollisionWithRectangle(player.body.getRectangle()) ||
		    this.foot.getCircle().detectCollisionWithCircle(player.head.getCircle()) ||
		    this.head.getCircle().detectCollisionWithCircle(player.foot.getCircle())) {
			
			if (this.foot.getCircle().detectCollisionWithCircle(player.head.getCircle()) && 
					this.foot.getCircle().getY() < player.head.getCircle().getY()) {
				verticalSpeed = 0;
				onTop = true;
			} else {
				onTop = false;
			}
			
			double push = (this.head.getCircle().getX() < player.head.getCircle().getX()) 
					? -player.stats.getSpeed() 
					: player.stats.getSpeed();
			translate(push, 0);
			collideWithEdges();
		} else {
			onTop = false;
		}
	}

	public void collideWithEdges() {
		if (head.getCircle().getX() - head.getCircle().getRadius() < 0) {
			double overflow = -(head.getCircle().getX() - head.getCircle().getRadius());
			translate(overflow, 0);
		}
		if (head.getCircle().getX() + head.getCircle().getRadius() > Field.WIDTH) {
			double overflow = (head.getCircle().getX() + head.getCircle().getRadius()) - Field.WIDTH;
			translate(-overflow, 0);
		}
	}
	
	public void collideWithGoal(Goal goal) {
		if (this.head.getCircle().detectCollisionWithRectangle(goal.getCrossBar())) {
			verticalSpeed = 0;
		} else if (this.foot.getCircle().detectCollisionWithRectangle(goal.getCrossBar())) {
			verticalSpeed = 0;
			onTop = true;
		} else {
			onTop = false;
		}
	}

	public void collideMyFootWithBall(Ball ball){
		if(this.foot.getCircle().getX() >= ball.getCircle().getX() - ball.getCircle().getRadius() &&
		this.foot.getCircle().getX() <= ball.getCircle().getX() + ball.getCircle().getRadius() &&
		this.foot.getCircle().getY() + this.foot.getCircle().getRadius() >= ball.getCircle().getY() - ball.getCircle().getRadius()){
			onTop = true;
		}
	}

	public void tick() {
		if (head != null && head.isStunned()) {
			head.tick();
		}

		if (foot != null) {
			foot.tick(side);
		}
		moveVertical();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public Power getPower() {
		return power;
	}

	public void setPower(Power power) {
		this.power = power;
	}

	public void usePower(Ball ball, Player rival) {
		if (power != null && power.isReady()) {
			power.applyPower(ball, rival);
		}
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public Side getSide() {
		return side;
	}

	public void setSide(Side side) {
		this.side = side;
	}

	public Head getHead() {
		return head;
	}

	public void setHead(Head head) {
		this.head = head;
	}

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}

	public Foot getFoot() {
		return foot;
	}

	public void setFoot(Foot foot) {
		this.foot = foot;
	}

	public PlayerStats getStats() {
		return stats;
	}

	public void setStats(PlayerStats stats) {
		this.stats = stats;
	}

	public double getVerticalSpeed() {
		return verticalSpeed;
	}

	public void setVerticalSpeed(double verticalSpeed) {
		this.verticalSpeed = verticalSpeed;
	}

	public boolean isOnTop() {
		return onTop;
	}

	public void setOnTop(boolean onTop) {
		this.onTop = onTop;
	}
}
