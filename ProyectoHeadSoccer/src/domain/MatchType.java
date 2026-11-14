package domain;

/**
 * Tipos de partido: por tiempo o por goles.
 */
public enum MatchType {
    TIME("Por Tiempo"),
    GOALS("Por Goles");

    private final String displayName;

    MatchType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
