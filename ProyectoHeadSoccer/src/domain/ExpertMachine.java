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
    private final OffensiveMachine offensive;
    private final DefensiveMachine defensive;
    private final static int LOW_TIME_SECONDS = 15;
    private final static double POWER_BALL_DISTANCE = 45.0;
    private final static double FULL_POWER = 100.0;



    /**
     * Constructs an expert machine controller for the given pitch side.
     *
     * @param side Pitch side controlled by this AI
     */
     public ExpertMachine(Side side){
         super(side);
         this.defensive = new DefensiveMachine(side);
         this.offensive = new OffensiveMachine(side);
     }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Set<PlayerAction> chooseActions(MatchView match, PlayerView me, BallView ball, PlayerView rival) {
        int myGoals = match.getGoals(side);
        int rivalGoals = match.getGoals(side.opposite());
        int remaining = match.getRemainingSeconds();
        boolean lowTimeTie = (myGoals == rivalGoals) && remaining >= 0 && remaining <= LOW_TIME_SECONDS;

        MachineController chosen;
        if (rival != null && rival.hasAura()) {
            chosen = defensive;
        } else if (rival != null && (rival.isStunned() || PlayerEffect.KNOCKED_OUT.name().equals(rival.getActiveEffectName()))) {
            chosen = offensive;
        } else if (myGoals > rivalGoals && !lowTimeTie) {
            chosen = defensive;
        } else {
            chosen = offensive;
        }

        Set<PlayerAction> actions = EnumSet.noneOf(PlayerAction.class);
        actions.addAll(chosen.chooseActions(match, me, ball, rival));

        if (me.getPowerProgress() >= FULL_POWER) {
            double distBall = Math.hypot(me.getHeadShape().getX() - ball.getX(), me.getHeadShape().getY() - ball.getY());
            if (distBall < POWER_BALL_DISTANCE) {
                actions.add(PlayerAction.POWER);
            }
        }
        return actions;
    }
}
