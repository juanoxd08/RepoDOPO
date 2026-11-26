package domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Machine controller implementing the Ofensiva (offensive) profile prioritizing scoring.
 * Pursues the ball across the pitch, positions behind it, jumps, kicks, and executes powers.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class OffensiveMachine extends MachineController {
    private static final long serialVersionUID = 1L;
    private static final double ATTACK_OFFSET = 16.0;
    private static final double BACKTRACK_OFFSET = 26.0;
    private static final double MOVE_TOLERANCE = 4.0;
    private static final double JUMP_MAX_DX = 32.0;
    private static final double JUMP_HEAD_MARGIN_Y = 15.0;
    private static final double KICK_MAX_DX = 30.0;
    private static final double KICK_MAX_HEIGHT = 70.0;
    private static final double KICK_DIRECTION_MARGIN = -12.0;
    private static final double POWER_BALL_DISTANCE = 50.0;
    private static final double FULL_POWER = 100.0;
    private static final double CROSSBAR_BACKOFF = 45.0;

    /**
     * Constructs an offensive machine controller for the given pitch side.
     *
     * @param side Pitch side controlled by this AI
     */
    public OffensiveMachine(Side side) {
        super(side);
    }

    /**
     * Attacks the opponent's goal:
     * <ul>
     *   <li>If the ball lies on top of a crossbar, steps back so the ball can roll off.</li>
     *   <li>Otherwise moves behind the ball relative to the opponent's goal
     *       (a bit further back when the ball is already behind the player).</li>
     *   <li>Jumps for headers when the ball is at head height or above.</li>
     *   <li>Kicks when the ball is within reach and not behind the player.</li>
     *   <li>Activates the power when the bar is full and the ball is close and in front.</li>
     * </ul>
     *
     * @param match Read-only match view
     * @param me Read-only view of the controlled player
     * @param ball Read-only ball view
     * @param rival Read-only view of the opponent
     * @return Actions to perform during this tick
     */
    @Override
    protected Set<PlayerAction> chooseActions(MatchView match, PlayerView me, BallView ball, PlayerView rival) {
        Set<PlayerAction> actions = EnumSet.noneOf(PlayerAction.class);
        double myX = me.getHeadShape().getX();
        double bx = ball.getX();

        boolean ballBehindMe = (bx - myX) * side.dir() < 0;

        double targetX = ballBehindMe
                ? bx - side.dir() * BACKTRACK_OFFSET
                : bx - side.dir() * ATTACK_OFFSET;

        if (isBallOnCrossBar(match, ball)) {
            moveToward(myX, bx - side.dir() * CROSSBAR_BACKOFF, MOVE_TOLERANCE, actions);
            return actions;
        }

        moveToward(myX, targetX, MOVE_TOLERANCE, actions);

        if (shouldJumpForBall(me, ball, JUMP_MAX_DX, JUMP_HEAD_MARGIN_Y)) {
            actions.add(PlayerAction.JUMP);
        }

        if (shouldKickBall(match, me, ball, KICK_MAX_DX, KICK_MAX_HEIGHT)
                && (bx - myX) * side.dir() > KICK_DIRECTION_MARGIN) {
            actions.add(PlayerAction.KICK);
        }

        if (me.getPowerProgress() >= FULL_POWER) {
            double distBall = Math.hypot(myX - bx, me.getHeadShape().getY() - ball.getY());
            if (distBall <= POWER_BALL_DISTANCE && (bx - myX) * side.dir() >= 0) {
                actions.add(PlayerAction.POWER);
            }
        }

        return actions;
    }

    /*
     * Checks whether the ball is lying on top of either crossbar, where it cannot be kicked.
     */
    private boolean isBallOnCrossBar(MatchView match, BallView ball) {
        for (Side s : Side.values()) {
            Rectangle bar = match.getCrossBar(s);
            boolean above = ball.getY() < bar.getY();
            boolean overBar = ball.getX() >= bar.getX() - ball.getRadius()
                    && ball.getX() <= bar.getX() + bar.getWidth() + ball.getRadius();
            if (above && overBar) return true;
        }
        return false;
    }
}
