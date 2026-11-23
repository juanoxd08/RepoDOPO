package domain;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

/**
 * Machine controller implementing the Torpe (clumsy) profile with randomized,
 * erratic movements and delayed reactions.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class ClumsyMachine extends MachineController {
    private static final long serialVersionUID = 1L;
    private static final int MIN_ACTION_TICKS = 12;
    private static final int EXTRA_RANDOM_TICKS = 18;
    private static final double POWER_CHANCE = 0.10;
    private static final double FULL_POWER = 100.0;
    private static final int CHOICES = 6;

    private final Random random;
    private int actionTimer = 0;
    private final Set<PlayerAction> bufferedActions = EnumSet.noneOf(PlayerAction.class);

    /**
     * Constructs a clumsy machine controller with a default random generator.
     *
     * @param side Pitch side controlled by this AI
     */
    public ClumsyMachine(Side side) {
        this(side, new Random());
    }

    /**
     * Constructs a clumsy machine controller with an injectable random generator for testing.
     *
     * @param side Pitch side controlled by this AI
     * @param random Random generator instance
     */
    public ClumsyMachine(Side side, Random random) {
        super(side);
        this.random = (random != null) ? random : new Random();
    }

    /**
     * Picks a random action and keeps it for 12 to 29 ticks, so the machine looks clumsy instead of shaking.
     * The possible choices are: move left, move right, jump, kick, move roughly toward the ball
     * (sometimes the wrong way) with an occasional kick, or stay idle for a moment.
     * When the power bar is full it activates the power with a 10% chance per decision.
     *
     * @param match Read-only match view
     * @param me Read-only view of the controlled player
     * @param ball Read-only ball view
     * @param rival Read-only view of the opponent
     * @return Actions to perform during this tick
     */
    @Override
    protected Set<PlayerAction> chooseActions(MatchView match, PlayerView me, BallView ball, PlayerView rival) {
        if (actionTimer > 0) {
            actionTimer--;
            return EnumSet.copyOf(bufferedActions);
        }

        bufferedActions.clear();
        actionTimer = MIN_ACTION_TICKS + random.nextInt(EXTRA_RANDOM_TICKS);

        int choice = random.nextInt(CHOICES);
        switch (choice) {
            case 0:
                bufferedActions.add(PlayerAction.LEFT);
                break;
            case 1:
                bufferedActions.add(PlayerAction.RIGHT);
                break;
            case 2:
                bufferedActions.add(PlayerAction.JUMP);
                break;
            case 3:
                bufferedActions.add(PlayerAction.KICK);
                break;
            case 4:
                if (ball.getX() > me.getHeadShape().getX()) {
                    bufferedActions.add(random.nextBoolean() ? PlayerAction.RIGHT : PlayerAction.LEFT);
                } else {
                    bufferedActions.add(random.nextBoolean() ? PlayerAction.LEFT : PlayerAction.RIGHT);
                }
                if (random.nextBoolean()) {
                    bufferedActions.add(PlayerAction.KICK);
                }
                break;
            case 5:
            default:
                break;
        }

        if (me.getPowerProgress() >= FULL_POWER && random.nextDouble() < POWER_CHANCE) {
            bufferedActions.add(PlayerAction.POWER);
        }

        return EnumSet.copyOf(bufferedActions);
    }
}
