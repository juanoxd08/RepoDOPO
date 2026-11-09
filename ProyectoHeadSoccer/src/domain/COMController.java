package domain;

import java.util.HashSet;
import java.util.Set;

/**
 * Autonomous artificial intelligence controller that decides actions for computer-controlled players.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class COMController implements Controller {
    private COMState state;
    private Side side;

    /**
     * Constructs an AI controller assigned to a field side.
     * 
     * @param side Side of the field defended by this controller
     */
    public COMController(Side side) {
        this.state = COMState.ATTACK;
        this.side = side;
    }

    /**
     * Gets the current tactical state of the machine.
     * 
     * @return Tactical state (ATTACK or DEFEND)
     */
    public COMState getState() {
        return state;
    }

    /**
     * Sets the tactical state of the machine.
     * 
     * @param state New tactical state
     */
    public void setState(COMState state) {
        this.state = state;
    }

    /**
     * Gets the side of the field defended by this controller.
     * 
     * @return Defended side
     */
    public Side getSide() {
        return side;
    }

    /**
     * Sets the side of the field defended by this controller.
     * 
     * @param side New defended side
     */
    public void setSide(Side side) {
        this.side = side;
    }

    /**
     * Evaluates current match conditions and decides actions to execute.
     * 
     * @param match Read-only inspection of the current match state
     * @return Set of decided actions for the current frame
     */
    @Override
    public Set<PlayerAction> decide(ReaderMatch match) {
        Set<PlayerAction> actions = new HashSet<>();
        if (match == null || match.getBall() == null) return actions;

        Player me = (match.getPlayer1() != null && match.getPlayer1().getSide() == this.side) ? match.getPlayer1() : match.getPlayer2();
        if (me == null || me.getHead() == null || me.getFoot() == null) return actions;

        Player rival = (me == match.getPlayer1()) ? match.getPlayer2() : match.getPlayer1();
        Ball ball = match.getBall();
        int sign = (side == Side.RIGHT) ? 1 : -1;

        if ((me.getHead().getCircle().getX() > ball.getCircle().getX() && side == Side.LEFT) ||
            (me.getHead().getCircle().getX() < ball.getCircle().getX() && side == Side.RIGHT)) {
            state = COMState.DEFEND; 
        } else {
            state = COMState.ATTACK;
        }
            
        if (state == COMState.ATTACK) {
            double targetX = ball.getCircle().getX() + (sign * 25);

            if (me.getHead().getCircle().getX() > targetX + 5) {
                actions.add(PlayerAction.LEFT);
            } else if (me.getHead().getCircle().getX() < targetX - 5) {
                actions.add(PlayerAction.RIGHT);
            }

            if (Math.abs(me.getFoot().getCircle().getX() - ball.getCircle().getX()) < 20 &&
                    ball.getCircle().getY() > Field.HEIGHT - 40) {
                actions.add(PlayerAction.KICK);
            }

            if (Math.abs(me.getHead().getCircle().getX() - ball.getCircle().getX()) < 30 &&
                    me.getHead().getCircle().getY() + 10 > ball.getCircle().getY()) {
                actions.add(PlayerAction.JUMP);
            }

            if (me.getPower() != null && me.getPower().isReady() &&
                    Math.abs(me.getFoot().getCircle().getX() - ball.getCircle().getX()) < 30) {
                actions.add(PlayerAction.POWER);
            }

        } else {
            if (side == Side.LEFT) {
                actions.add(PlayerAction.LEFT);
            } else {
                actions.add(PlayerAction.RIGHT);
            }

            if (ball.getCircle().getY() > Field.HEIGHT - 60) {
                actions.add(PlayerAction.JUMP);
            }
        }

        return actions;
    }
}
