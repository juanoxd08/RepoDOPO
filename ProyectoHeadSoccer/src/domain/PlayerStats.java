package domain;

public class PlayerStats {
	private int jump;
	private int kick;
	private int speed;

	public PlayerStats(int jump, int kick, int speed) {
		this.jump = jump;
		this.kick = kick;
		this.speed = speed;
	}

	public PlayerStats() {
		this.jump = 1;
		this.kick = 1;
		this.speed = 1;
	}


	public int getJump() {
		return jump;
	}
	public void setJump(int jump) throws HeadSoccerException {
		if(jump < 1 || jump > 5) {
			throw new HeadSoccerException(HeadSoccerException.PLAYER_STATS_OUT_OF_BOUNDS);
		}
		this.jump = jump;	
	}
	public int getKick() {
		return kick;
	}
	public void setKick(int kick) throws HeadSoccerException {
		if(kick < 1 || kick > 5) {
			throw new HeadSoccerException(HeadSoccerException.PLAYER_STATS_OUT_OF_BOUNDS);
		}
		this.kick = kick;
	}

	public int getSpeed() {
		return speed;
	}
	public void setSpeed(int speed)throws HeadSoccerException {
		if(speed < 1 || speed > 5) {
			throw new HeadSoccerException(HeadSoccerException.PLAYER_STATS_OUT_OF_BOUNDS);
		}
		this.speed = speed;
	}

	public double speedPx(){
		return speed * 10.0;
	}

	public double jumpPx(){
		return jump * 10.0;
	}

	public double kickPx(){
		return kick * 10.0;
	}
	
	
	
}
