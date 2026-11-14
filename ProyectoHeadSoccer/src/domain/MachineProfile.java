package domain;

/**
 * Enumerates machine difficulty and tactical profiles as defined in the DOPOSoccer specification:
 * 1. TORPE: chooses movements randomly.
 * 2. OFENSIVA: prioritizes scoring goals.
 * 3. DEFENSIVA: prioritizes defending its goal area.
 * 4. EXPERTA: selects actions dynamically based on match context and score difference.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public enum MachineProfile {
    TORPE("Torpe"),
    OFENSIVA("Ofensiva"),
    DEFENSIVA("Defensiva"),
    EXPERTA("Experta");

    private final String displayName;

    MachineProfile(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
