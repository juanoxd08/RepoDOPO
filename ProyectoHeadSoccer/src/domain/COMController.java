package domain;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Autonomous artificial intelligence controller that implements the 4 machine profiles:
 * Torpe, Ofensiva, Defensiva, and Experta.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class COMController implements Controller {
    private COMState state;
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
        this.state = COMState.ATTACK;
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
    public Set<PlayerAction> decide(ReaderMatch match) {
        Set<PlayerAction> actions = new HashSet<>();
        if (match == null || match.getBall() == null) return actions;

        Player me = (match.getPlayer1() != null && match.getPlayer1().getSide() == this.side) ? match.getPlayer1() : match.getPlayer2();
        if (me == null || me.getHead() == null || me.getFoot() == null) return actions;

        Player rival = (me == match.getPlayer1()) ? match.getPlayer2() : match.getPlayer1();
        Ball ball = match.getBall();

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

    private Set<PlayerAction> decideTorpe(Player me, Ball ball) {
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

        if (me.getPower() != null && me.getPower().isReady() && random.nextInt(10) == 0) {
            bufferedRandomActions.add(PlayerAction.POWER);
        }

        return bufferedRandomActions;
    }

    private Set<PlayerAction> decideOfensiva(Player me, Ball ball, Player rival) {
        Set<PlayerAction> actions = new HashSet<>();
        double myX = me.getHead().getCircle().getX();
        double bx = ball.getCircle().getX();
        double by = ball.getCircle().getY();

        double attackOffset = (side == Side.RIGHT) ? 18.0 : -18.0;
        double targetX = bx + attackOffset;

        if (myX > targetX + 4) actions.add(PlayerAction.LEFT);
        else if (myX < targetX - 4) actions.add(PlayerAction.RIGHT);

        if (Math.abs(myX - bx) < 32 && by < me.getHead().getCircle().getY() + 15) {
            actions.add(PlayerAction.JUMP);
        }

        if (Math.abs(me.getFoot().getCircle().getX() - bx) < 26 && by > Field.HEIGHT - 70) {
            actions.add(PlayerAction.KICK);
        }

        if (me.getPower() != null && me.getPower().isReady()) {
            actions.add(PlayerAction.POWER);
        }

        return actions;
    }

    private Set<PlayerAction> decideDefensiva(Player me, Ball ball, Player rival) {
        Set<PlayerAction> actions = new HashSet<>();
        double myX = me.getHead().getCircle().getX();
        double bx = ball.getCircle().getX();
        double by = ball.getCircle().getY();

        double homeX = (side == Side.RIGHT) ? Field.WIDTH - 70 : 70;

        // Se mantiene cerca de su arco si el balón está lejos
        if (Math.abs(bx - homeX) > 130) {
            if (myX > homeX + 10) actions.add(PlayerAction.LEFT);
            else if (myX < homeX - 10) actions.add(PlayerAction.RIGHT);
        } else {
            // El balón se acerca peligrosamente: interceptar y despejar
            if (myX > bx + 12) actions.add(PlayerAction.LEFT);
            else if (myX < bx - 12) actions.add(PlayerAction.RIGHT);

            if (Math.abs(myX - bx) < 30 && by < me.getHead().getCircle().getY() + 10) {
                actions.add(PlayerAction.JUMP);
            }
            if (Math.abs(me.getFoot().getCircle().getX() - bx) < 28) {
                actions.add(PlayerAction.KICK);
            }
        }

        return actions;
    }

    private Set<PlayerAction> decideExperta(ReaderMatch match, Player me, Ball ball, Player rival) {
        int myScore = (side == Side.LEFT) ? match.getScoreBoard().getPoints()[0] : match.getScoreBoard().getPoints()[1];
        int rivalScore = (side == Side.LEFT) ? match.getScoreBoard().getPoints()[1] : match.getScoreBoard().getPoints()[0];

        // Si va perdiendo, adopta postura ofensiva agresiva
        if (myScore < rivalScore) {
            return decideOfensiva(me, ball, rival);
        }

        // Táctica balanceada con timing de poder
        Set<PlayerAction> actions = decideOfensiva(me, ball, rival);

        // Uso estratégico del poder solo cuando el balón está cerca y en línea de tiro
        if (me.getPower() != null && me.getPower().isReady()) {
            double distBall = Math.hypot(me.getHead().getCircle().getX() - ball.getCircle().getX(), 
                                         me.getHead().getCircle().getY() - ball.getCircle().getY());
            if (distBall < 45) {
                actions.add(PlayerAction.POWER);
            }
        }

        return actions;
    }
}
