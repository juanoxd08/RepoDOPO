package domain;

public class TimeRule implements MatchRule{
    private int timeRemaining;
    private int tickCount;


    public TimeRule(){
        this.timeRemaining = 60;
        this.tickCount = 0;
    }

    public TimeRule(int minutes) throws DOPOSoccerException{
        if(minutes <= 0) throw new DOPOSoccerException(DOPOSoccerException.INVALID_MATCH_CONFIG);
        this.timeRemaining = minutes* 60;
        this.tickCount = 0;
    }

    @Override 
    public void onTick(Match match){
        if(match.getStatus() == GameStatus.PLAYING){
            if (timeRemaining > 0) {
                tickCount++;
                if (tickCount >= Physics.FPS) {
                    timeRemaining--;
                    tickCount = 0;
                }
            }else if(match.getScoreBoard().isTie()){
                match.setState(GameStatus.SUDDEN_DEATH);
            }
        }   
    }

    @Override 
    public boolean isOver(Match match){
        return timeRemaining <= 0 && !match.getScoreBoard().isTie();
    }

    @Override
    public int remainingSeconds(Match match){
        return timeRemaining;
    }
}
