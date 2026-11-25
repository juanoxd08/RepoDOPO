package domain;

/**
 * Goal-based match rule: first player to reach targetGoals wins.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class GoalRule implements MatchRule {
    private final int targetGoals;

    /**
     * Constructs a goal rule with the given target score.
     *
     * @param targetGoals Positive number of goals required to win
     * @throws DOPOSoccerException if targetGoals is not positive
     */
    public GoalRule(int targetGoals) throws DOPOSoccerException {
        if (targetGoals <= 0) {
            throw new DOPOSoccerException(DOPOSoccerException.INVALID_MATCH_CONFIG);
        }
        this.targetGoals = targetGoals;
    }

    /**
     * Advances per-tick rule logic. Goal-based matches have no timer countdown.
     *
     * @param match Active match instance
     */
    @Override
    public void onTick(Match match) {
    }

    /**
     * Checks whether either side has reached the configured target goal count.
     *
     * @param match Active match instance
     * @return True if either side reached targetGoals
     */
    @Override
    public boolean isOver(Match match) {
        return match.getGoals(Side.LEFT) >= targetGoals || match.getGoals(Side.RIGHT) >= targetGoals;
    }

    /**
     * Returns -1 since goal-based matches do not track remaining seconds.
     *
     * @param match Active match instance
     * @return Always -1
     */
    @Override
    public int remainingSeconds(Match match) {
        return -1;
    }
}
