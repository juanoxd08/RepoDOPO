package domain;

/**
 * Match termination rule types (by time or by goals).
 * 
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public enum MatchType {
    TIME("Por Tiempo"),
    GOALS("Por Goles");

    private final String displayName;

    MatchType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the localized display label for this match type.
     * 
     * @return Display name
     */
    public String getDisplayName() {
        return displayName;
    }
}
