package domain;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Autonomous artificial intelligence controller that implements the 4 machine profiles:
 * Torpe, Ofensiva, Defensiva, and Experta.
 * 
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class COMController implements Controller {
    private Side side;
    private MachineProfile profile;
    private Random random = new Random();
    private int randomActionTimer = 0;
    private Set<PlayerAction> bufferedRandomActions = new HashSet<>();

    public COMController(Side side) {
        this(side, MachineProfile.EXPERTA);
    }

    public COMController(Side side, MachineProfile profile) {
        this.side = side;
        this.profile = (profile != null) ? profile : MachineProfile.EXPERTA;
    }

    public MachineProfile getProfile() {
        return profile;
    }

    public void setProfile(MachineProfile profile) {
        this.profile = profile;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    @Override
    public Set<PlayerAction> decide(MatchView match) {
        Set<PlayerAction> actions = new HashSet<>();
        if (match == null || match.getBall() == null) return actions;

        PlayerView me = match.getPlayer(this.side);
        if (me == null || me.getHeadShape() == null || me.getFootShape() == null) return actions;

        PlayerView rival = match.getPlayer(this.side.opposite());
        BallView ball = match.getBall();

        switch (profile) {
            case TORPE:
                return decideTorpe(me, ball);
            case OFENSIVA:
                return decideOfensiva(me, ball, rival);
            case DEFENSIVA:
                return decideDefensiva(me, ball, rival);
            case EXPERTA:
            default:
                return decideExperta(match, me, ball, rival);
        }
    }

    private Set<PlayerAction> decideTorpe(PlayerView me, BallView ball) {
        if (randomActionTimer > 0) {
            randomActionTimer--;
            return bufferedRandomActions;
        }

        bufferedRandomActions.clear();
        randomActionTimer = 10 + random.nextInt(20);

        int choice = random.nextInt(6);
        if (choice == 0) bufferedRandomActions.add(PlayerAction.LEFT);
        else if (choice == 1) bufferedRandomActions.add(PlayerAction.RIGHT);
        else if (choice == 2) bufferedRandomActions.add(PlayerAction.JUMP);
        else if (choice == 3) bufferedRandomActions.add(PlayerAction.KICK);
        else if (choice == 4) {
            bufferedRandomActions.add(PlayerAction.LEFT);
            bufferedRandomActions.add(PlayerAction.KICK);
        }

        if (me.getPowerProgress() >= 100.0 && random.nextInt(10) == 0) {
            bufferedRandomActions.add(PlayerAction.POWER);
        }

        return bufferedRandomActions;
    }

    private Set<PlayerAction> decideOfensiva(PlayerView me, BallView ball, PlayerView rival) {
        Set<PlayerAction> actions = new HashSet<>();
        double myX = me.getHeadShape().getX();
        double bx = ball.getX();
        double by = ball.getY();

        double attackOffset = (side == Side.RIGHT) ? 18.0 : -18.0;
        double targetX = bx + attackOffset;

        if (myX > targetX + 4) actions.add(PlayerAction.LEFT);
        else if (myX < targetX - 4) actions.add(PlayerAction.RIGHT);

        if (Math.abs(myX - bx) < 32 && by < me.getHeadShape().getY() + 15) {
            actions.add(PlayerAction.JUMP);
        }

        if (Math.abs(me.getFootShape().getX() - bx) < 26 && by > Field.HEIGHT - 70) {
            actions.add(PlayerAction.KICK);
        }

        if (me.getPowerProgress() >= 100.0) {
            actions.add(PlayerAction.POWER);
        }

        return actions;
    }

    private Set<PlayerAction> decideDefensiva(PlayerView me, BallView ball, PlayerView rival) {
        Set<PlayerAction> actions = new HashSet<>();
        double myX = me.getHeadShape().getX();
        double bx = ball.getX();
        double by = ball.getY();

        double homeX = (side == Side.RIGHT) ? Field.WIDTH - 70 : 70;

        if (Math.abs(bx - homeX) > 130) {
            if (myX > homeX + 10) actions.add(PlayerAction.LEFT);
            else if (myX < homeX - 10) actions.add(PlayerAction.RIGHT);
        } else {
            if (myX > bx + 12) actions.add(PlayerAction.LEFT);
            else if (myX < bx - 12) actions.add(PlayerAction.RIGHT);

            if (Math.abs(myX - bx) < 30 && by < me.getHeadShape().getY() + 10) {
                actions.add(PlayerAction.JUMP);
            }
            if (Math.abs(me.getFootShape().getX() - bx) < 28) {
                actions.add(PlayerAction.KICK);
            }
        }

        return actions;
    }

    private Set<PlayerAction> decideExperta(MatchView match, PlayerView me, BallView ball, PlayerView rival) {
        int myScore = match.getGoals(side);
        int rivalScore = match.getGoals(side.opposite());

        if (myScore < rivalScore) {
            return decideOfensiva(me, ball, rival);
        }

        Set<PlayerAction> actions = decideOfensiva(me, ball, rival);

        if (me.getPowerProgress() >= 100.0) {
            double distBall = Math.hypot(me.getHeadShape().getX() - ball.getX(), 
                                         me.getHeadShape().getY() - ball.getY());
            if (distBall < 45) {
                actions.add(PlayerAction.POWER);
            }
        }

        return actions;
    }
}
