package domain;

import java.util.Optional;

public interface MatchRule {
    void onTick(Match match);
    boolean isOver(Match match);
    Optional<Side> winner(Match match);
    int remainingSeconds(Match match);
}