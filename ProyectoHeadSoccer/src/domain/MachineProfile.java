package domain;

import java.util.Random;
import java.util.function.Function;

/**
 * Enumerates machine difficulty and tactical profiles as defined in the DOPOSoccer specification:
 * 1. TORPE: chooses movements randomly.
 * 2. OFENSIVA: prioritizes scoring goals.
 * 3. DEFENSIVA: prioritizes defending its goal area.
 * 4. EXPERTA: selects actions dynamically based on match context and score difference.
 * 
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public enum MachineProfile {
    TORPE("Torpe", ClumsyMachine::new),
    OFENSIVA("Ofensiva", OffensiveMachine::new),
    DEFENSIVA("Defensiva", DefensiveMachine::new),
    EXPERTA("Experta", ExpertMachine::new);

    private final String displayName;
    private final Function<Side, MachineController> factory;

    MachineProfile(String displayName, Function<Side, MachineController> factory) {
        this.displayName = displayName;
        this.factory = factory;
    }

    /**
     * Returns the localized display name of this machine profile.
     *
     * @return Profile display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Creates a new machine controller instance for the given pitch side.
     *
     * @param side Pitch side controlled by the AI
     * @return New MachineController instance
     */
    public MachineController create(Side side) {
        return factory.apply(side);
    }

    /**
     * Creates a new machine controller instance using an injectable random generator
     * for deterministic testing of the TORPE profile.
     *
     * @param side Pitch side controlled by the AI
     * @param random Random generator instance
     * @return New MachineController instance
     */
    public MachineController create(Side side, Random random) {
        if (this == TORPE) {
            return new ClumsyMachine(side, random);
        }
        return create(side);
    }
}
