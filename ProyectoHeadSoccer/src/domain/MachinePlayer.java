package domain;

public class MachinePlayer extends Player{
	public void act(Ball ball, Player Rival){
		int targetX;
		if (this.getSide() == Side.RIGHT) {
		    targetX = ball.getCircle().x + 25;
		} else {
		    targetX = ball.getCircle().x - 25;
		}
		
		if(this.getHead().getCircle().x > targetX + 5) {
			moveLeft();
		}else if (this.getHead().getCircle().x < targetX - 5){
			moveRight();
		}
		
		if(Math.abs(this.getFoot().getCircle().x - ball.getCircle().x) < 20 &&
		   ball.getCircle().y > Field.HEIGHT - 40) {
			kick();		
		}else {
			stopKick();
		}
		
		
		if (Math.abs(this.getHead().getCircle().x - ball.getCircle().x) < 30 &&
				this.getHead().getCircle().y + 10 > ball.getCircle().y) {

			jump();
		}
		
		if (this.getPower() != null && this.getPower().isReady() && 
			    Math.abs(this.getFoot().getCircle().x - ball.getCircle().x) < 30) {
			    this.usePower(ball, Rival);
		}
		
	}
}
