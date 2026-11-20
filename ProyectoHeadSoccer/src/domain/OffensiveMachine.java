package domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Machine controller implementing the Ofensiva (offensive) profile prioritizing scoring.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class OffensiveMachine extends MachineController {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an offensive machine controller for the given pitch side.
     *
     * @param side Pitch side controlled by this AI
     */
    public OffensiveMachine(Side side) {
        super(side);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Set<PlayerAction> chooseActions(MatchView match, PlayerView me, BallView ball, PlayerView rival) {
        return EnumSet.noneOf(PlayerAction.class);
    }
}
