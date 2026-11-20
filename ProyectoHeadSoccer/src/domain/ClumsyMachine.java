package domain;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

/**
 * Machine controller implementing the Torpe (clumsy) profile with randomized actions.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class ClumsyMachine extends MachineController {
    private static final long serialVersionUID = 1L;
    private final Random random;

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
     * {@inheritDoc}
     */
    @Override
    protected Set<PlayerAction> chooseActions(MatchView match, PlayerView me, BallView ball, PlayerView rival) {
        return EnumSet.noneOf(PlayerAction.class);
    }
}
