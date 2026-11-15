package domain;

import java.util.Optional;

public interface MatchRule {
    void onTick(Match match);
    boolean isOver(Match match);
    public default Optional<Side> winner(Match match){
        if (!isOver(match)) {
            return Optional.empty();
        }
        return match.getScoreBoard().getLeader();
    }
    int remainingSeconds(Match match);
}