package domain;

import java.util.Optional;

/**
 * Goal-based match rule: first player to reach targetGoals wins.
 */
public class GoalRule implements MatchRule {
    private final int targetGoals;

    public GoalRule(int targetGoals) throws DOPOSoccerException {
        if (targetGoals <= 0) {
            throw new DOPOSoccerException(DOPOSoccerException.INVALID_MATCH_CONFIG);
        }
        this.targetGoals = targetGoals;
    }

    @Override
    public void onTick(Match match) {
    }

    @Override
    public boolean isOver(Match match) {
        return match.getGoals(Side.LEFT) >= targetGoals || match.getGoals(Side.RIGHT) >= targetGoals;
    }

    @Override
    public Optional<Side> winner(Match match) {
        if (!isOver(match)) {
            return Optional.empty();
        }
        return match.getScoreBoard().getLeader();
    }

    @Override
    public int remainingSeconds(Match match) {
        return -1;
    }
}
