package domain;

import java.util.HashSet;
import java.util.Set;

public class COMController implements Controller {
    private COMState state;
    private Side side;

    public COMController(Side side) {
        this.state = COMState.ATTACK;
        this.side = side;
    }

    public COMState getState() {
        return state;
    }

    public void setState(COMState state) {
        this.state = state;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

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

            // Solo salta si el balon esta bajo para pasarlo por encima sin cabecear hacia su propio arco
            if (ball.getCircle().getY() > Field.HEIGHT - 60) {
                actions.add(PlayerAction.JUMP);
            }
        }

        return actions;
    }

    
}
