package domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Machine controller implementing the Experta (expert) profile adapting tactics to match state.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class ExpertMachine extends MachineController {
    private static final long serialVersionUID = 1L;
    private static final int LOW_TIME_SECONDS = 15;
    private static final double POWER_BALL_DISTANCE = 45.0;
    private static final double FULL_POWER = 100.0;
    private static final double OWN_HALF_FACTOR = 0.45;

    private final OffensiveMachine offensive;
    private final DefensiveMachine defensive;

    /**
     * Constructs an expert machine controller for the given pitch side.
     *
     * @param side Pitch side controlled by this AI
     */
    public ExpertMachine(Side side) {
        super(side);
        this.defensive = new DefensiveMachine(side);
        this.offensive = new OffensiveMachine(side);
    }

    /**
     * Chooses between the defensive and the offensive behavior, in this order:
     * <ul>
     *   <li>Defends if a power shot is coming toward its goal or the rival has the aura.</li>
     *   <li>Attacks if the rival is stunned or affected by any power status effect.</li>
     *   <li>In sudden death, defends while the ball is in its own half and attacks otherwise.</li>
     *   <li>Defends while winning, unless the score is tied with little time left.</li>
     *   <li>Attacks in any other case.</li>
     * </ul>
     * It also activates the power when the bar is full and the ball is close and in front.
     *
     * @param match Read-only match view
     * @param me Read-only view of the controlled player
     * @param ball Read-only ball view
     * @param rival Read-only view of the opponent
     * @return Actions to perform during this tick
     */
    @Override
    protected Set<PlayerAction> chooseActions(MatchView match, PlayerView me, BallView ball, PlayerView rival) {
        int myGoals = match.getGoals(side);
        int rivalGoals = match.getGoals(side.opposite());
        int remaining = match.getRemainingSeconds();
        boolean lowTimeTie = (myGoals == rivalGoals) && remaining >= 0 && remaining <= LOW_TIME_SECONDS;

        MachineController chosen;
        boolean ballPowerThreat = ball.hasPowerShot() && (ball.getHorizontalSpeed() * side.dir() < 0);
        boolean rivalUnderEffect = rival != null && (rival.isStunned()
                || (rival.getActiveEffect() != null && rival.getActiveEffect() != PlayerEffect.NONE));

        if (ballPowerThreat || (rival != null && rival.hasAura())) {
            chosen = defensive;
        } else if (rivalUnderEffect) {
            chosen = offensive;
        } else if (match.isSuddenDeath()) {
            double distToGoal = Math.abs(ball.getX() - ownGoalX(match));
            chosen = (distToGoal < match.getFieldWidth() * OWN_HALF_FACTOR) ? defensive : offensive;
        } else if (myGoals > rivalGoals && !lowTimeTie) {
            chosen = defensive;
        } else {
            chosen = offensive;
        }

        Set<PlayerAction> actions = EnumSet.noneOf(PlayerAction.class);
        actions.addAll(chosen.chooseActions(match, me, ball, rival));

        if (me.getPowerProgress() >= FULL_POWER) {
            double myX = me.getHeadShape().getX();
            double distBall = Math.hypot(myX - ball.getX(), me.getHeadShape().getY() - ball.getY());
            if (distBall <= POWER_BALL_DISTANCE && (ball.getX() - myX) * side.dir() >= 0) {
                actions.add(PlayerAction.POWER);
            }
        }
        return actions;
    }
}
