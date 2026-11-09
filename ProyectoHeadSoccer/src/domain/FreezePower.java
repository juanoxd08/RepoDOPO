package domain;

public class FreezePower extends Power {

	@Override
	public void applyPower(Ball ball, Player rival) {
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
	public void affectBall(Ball ball, Side side) {
		double dir = (side == Side.LEFT) ? 1.0 : -1.0;
		if (ball.getCircle().getY() > 40) {
			ball.setVerticalSpeed(-25);
			ball.setHorizontalSpeed(10 * dir);
		} else {
			ball.setVerticalSpeed(10);
		}
	}
}
