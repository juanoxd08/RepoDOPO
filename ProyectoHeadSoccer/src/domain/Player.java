package domain;

public class Player implements Collidable{
	private String Name;
	private Country country;
	private Side side;
	private Power power; 
	private Head head;
	private Body body;
	private Foot foot;
	private PlayerStats stats;
	private int verticalSpeed;
	private boolean onTop;
	private int points;
	public static final int GRAVITY = 2;
	
	
	public void moveLeft() {
		if (!head.isStunned() && head.getCircle().x - head.getCircle().getRadious() > 0) {
			head.getCircle().x -= stats.getSpeed();
			body.getRectangle().x -= stats.getSpeed();
			foot.getCircle().x -= stats.getSpeed();
		}
	}
	
	public void moveRight(){
		if (!head.isStunned() && head.getCircle().x + head.getCircle().getRadious() < Field.WIDTH) {
			head.getCircle().x += stats.getSpeed();
			body.getRectangle().x += stats.getSpeed();		
			foot.getCircle().x += stats.getSpeed();
		}
	}
	
	public void dashLeft(){
		if (!head.isStunned() && head.getCircle().x - head.getCircle().getRadious() > 0) {
			head.getCircle().x -= stats.getDash();
			body.getRectangle().x -= stats.getDash();
			foot.getCircle().x -= stats.getDash();
		}
		collideWithEdges();
	}
	
	public void dashRight(){
		if (!head.isStunned() && head.getCircle().x + head.getCircle().getRadious() < Field.WIDTH) {
			head.getCircle().x += stats.getDash();
			body.getRectangle().x += stats.getDash();
			foot.getCircle().x += stats.getDash();
		}
		collideWithEdges();
	}
	
	public void jump() {
	    if (!head.isStunned() && foot.getCircle().y + foot.getCircle().getRadious() >= Field.HEIGHT) {
	        verticalSpeed = -stats.getJump(); 
	    }
	}
	
	public void moveVertical(){
		head.getCircle().y += verticalSpeed;
		body.getRectangle().y += verticalSpeed;		
		foot.getCircle().y += verticalSpeed;
		if(this.foot.getCircle().y + this.foot.getCircle().getRadious() < Field.HEIGHT && !onTop) { 
			verticalSpeed += GRAVITY;
		} else {
			verticalSpeed = 0;
		}
	}
	
	public void kick(){
		if (!head.isStunned()) {
			foot.kicking(side);
		}
	}

	public void stopKick(){
		foot.stopKicking(side);
	}
	
	public void collideWithPlayer(Player player) {
		if(this.head.getCircle().detectCollisionWithCircle(player.head.getCircle()) ||
		   this.head.getCircle().detectCollisionWithRectangle(player.body.getRectangle()) ||
		   this.body.getRectangle().detectCollisionWithCircle(player.head.getCircle()) ||
		   this.body.getRectangle().detectCollisionWithRectangle(player.body.getRectangle()) ||
		   this.foot.getCircle().detectCollisionWithCircle(player.head.getCircle()) ||
		   this.head.getCircle().detectCollisionWithCircle(player.foot.getCircle())
				) {
			if(this.foot.getCircle().detectCollisionWithCircle(player.head.getCircle()) && this.foot.getCircle().y < player.head.getCircle().y) {
				verticalSpeed = 0;
				onTop = true;
			} else {
				onTop = false;
			}
			
			if (this.head.getCircle().x < player.head.getCircle().x) {
				this.head.getCircle().x = this.head.getCircle().x - player.stats.getSpeed();
				this.foot.getCircle().x = this.foot.getCircle().x - player.stats.getSpeed();
				this.body.getRectangle().x = this.body.getRectangle().x - player.stats.getSpeed();
			} else {
				this.head.getCircle().x = this.head.getCircle().x + player.stats.getSpeed();
				this.foot.getCircle().x = this.foot.getCircle().x + player.stats.getSpeed();
				this.body.getRectangle().x = this.body.getRectangle().x + player.stats.getSpeed();
			}
			collideWithEdges();
		} else {
			onTop = false;
		}
	}

	public void collideWithEdges() {
		if (head.getCircle().x - head.getCircle().getRadious() < 0) {
			int overflow = -(head.getCircle().x - head.getCircle().getRadious());
			head.getCircle().x += overflow;
			body.getRectangle().x += overflow;
			foot.getCircle().x += overflow;
		}
		if (head.getCircle().x + head.getCircle().getRadious() > Field.WIDTH) {
			int overflow = (head.getCircle().x + head.getCircle().getRadious()) - Field.WIDTH;
			head.getCircle().x -= overflow;
			body.getRectangle().x -= overflow;
			foot.getCircle().x -= overflow;
		}
	}
	
	public void collideWithGoal(Goal goal) {
		if(this.head.getCircle().detectCollisionWithRectangle(goal.getRectangle())) {
			verticalSpeed = 0;
		} else if(this.foot.getCircle().detectCollisionWithRectangle(goal.getRectangle())) {
			verticalSpeed = 0;
			onTop = true;
		} else {
			onTop = false;
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

	public Body getBody() {
		return body;
	}

	public Foot getFoot() {
		return foot;
	}

	public PlayerStats getStats() {
		return stats;
	}
}
