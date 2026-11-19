package domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Abstract Template Method base controller for autonomous machine players.
 * Validates match context before delegating tactical action selection to each profile subclass.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public abstract class MachineController implements Controller {
    private static final long serialVersionUID = 1L;
    protected final Side side;

    /**
     * Constructs a machine controller assigned to the given pitch side.
     *
     * @param side Pitch side controlled by this AI (defaults to RIGHT if null)
     */
    protected MachineController(Side side) {
        this.side = (side != null) ? side : Side.RIGHT;
    }

    /**
     * Returns the pitch side controlled by this machine.
     *
     * @return Assigned pitch side
     */
    public Side getSide() {
        return side;
    }

    /**
     * Template Method validating match state and delegating action selection to {@link #chooseActions}.
     *
     * @param match Read-only match view
     * @return Set of actions to execute on the current frame
     */
    @Override
    public final Set<PlayerAction> decide(MatchView match) {
        if (match == null || match.getBall() == null) {
            return EnumSet.noneOf(PlayerAction.class);
        }
        PlayerView me = match.getPlayer(side);
        PlayerView rival = match.getPlayer(side.opposite());
        if (me == null || me.getHeadShape() == null || me.getFootShape() == null) {
            return EnumSet.noneOf(PlayerAction.class);
        }
        return chooseActions(match, me, match.getBall(), rival);
    }

    /*
     * Adds LEFT or RIGHT movement toward targetX when outside the tolerance margin.
     */
    protected void moveToward(double meX, double targetX, double tolerance, Set<PlayerAction> actions) {
        if (meX > targetX + tolerance) {
            actions.add(PlayerAction.LEFT);
        } else if (meX < targetX - tolerance) {
            actions.add(PlayerAction.RIGHT);
        }
    }

    /**
     * Selects the intended actions for this frame based on the specific machine profile strategy.
     *
     * @param match Read-only match view
     * @param me Controlled player view
     * @param ball Ball view
     * @param rival Opponent player view (may be null)
     * @return Set of actions to perform
     */
    protected abstract Set<PlayerAction> chooseActions(MatchView match, PlayerView me, BallView ball, PlayerView rival);

    
}