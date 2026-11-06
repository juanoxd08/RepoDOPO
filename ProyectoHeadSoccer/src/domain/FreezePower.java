package domain;

public class FreezePower extends Power {

	@Override
	public void applyPower(Ball ball, Player rival){
		if (ball.getCircle().detectCollisionWithCircle(rival.getHead().getCircle()) ||
		    ball.getCircle().detectCollisionWithRectangle(rival.getBody().getRectangle()) ||
		    ball.getCircle().detectCollisionWithCircle(rival.getFoot().getCircle())) {
			rival.getHead().setStunned(true);
		}
	}

	@Override
	public void stopPower(Player rival) {
		rival.getHead().setStunned(false);
	}
	
	@Override
	public void affectBall(Ball ball, Side side){
		if(side == Side.LEFT) {
			if(ball.getCircle().y > 40) {
				ball.setVerticalSpeed(-25);
				ball.setHorizontalSpeed(10);
			}else {
				ball.setVerticalSpeed(10);
			}
		}else {
			if(ball.getCircle().y > 40) {
				ball.setVerticalSpeed(-25);
				ball.setHorizontalSpeed(-10);
			}else {
				ball.setVerticalSpeed(10);
			}	
		}
	}
}
