package domain;

public interface MatchListener{
    default void onGoal(Side scorer){}
    default void onStunned(Side victim){}
    default void onMatchFinished(Side winner, int winnerPoints, int loserPoints){}
}