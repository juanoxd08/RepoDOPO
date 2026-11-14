package domain;

/**
 * Read-only view of a player's state and defensive shape copies for rendering.
 */
public interface PlayerView {
    String getName();
    Country getCountry();
    Side getSide();
    int getGoals();
    double getLife();
    double getMaxLife();
    double getPowerProgress();
    boolean hasAura();
    boolean isStunned();
    boolean isKicking();
    String getActiveEffectName();
    Circle getHeadShape();
    Rectangle getBodyShape();
    Circle getFootShape();
    int getStat(StatType type);
    int getPoints();
}
