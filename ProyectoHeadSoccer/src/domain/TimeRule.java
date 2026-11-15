package domain;

/**
 * Time-based match rule with sudden-death golden goal activation on ties.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class TimeRule implements MatchRule{
    private int timeRemaining;
    private int tickCount;

    /**
     * Creates a default 1-minute time rule.
     */
    public TimeRule(){
        this.timeRemaining = 60;
        this.tickCount = 0;
    }

    /**
     * Creates a time rule for the given duration in minutes.
     *
     * @param minutes Match duration in minutes (must be positive)
     * @throws DOPOSoccerException if minutes is not positive
     */
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
