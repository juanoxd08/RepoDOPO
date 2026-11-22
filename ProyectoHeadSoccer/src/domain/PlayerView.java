package domain;

/**
 * Read-only view of a player's state and defensive shape copies for rendering.
 * 
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public interface PlayerView {

    /**
     * Returns the display name of the player.
     *
     * @return Player name
     */
    String getName();

    /**
     * Returns the country represented by the player.
     *
     * @return Selected Country
     */
    Country getCountry();

    /**
     * Returns the field side assigned to the player.
     *
     * @return Assigned Side
     */
    Side getSide();

    /**
     * Returns the player's current health points.
     *
     * @return Current health
     */
    double getLife();

    /**
     * Returns the player's maximum health points.
     *
     * @return Maximum health
     */
    double getMaxLife();

    /**
     * Returns the current power gauge charge percentage in [0.0, 100.0].
     *
     * @return Power gauge charge
     */
    double getPowerProgress();

    /**
     * Indicates whether the player's power aura is currently active.
     *
     * @return True if power aura is active
     */
    boolean hasAura();

    /**
     * Indicates whether the player is currently unable to act due to a stun or status effect.
     *
     * @return True if stunned
     */
    boolean isStunned();

    /**
     * Indicates whether the player is currently executing a kick animation.
     *
     * @return True if kicking
     */
    boolean isKicking();

    /**
     * Returns the display label of the active status effect, or an empty string if none.
     *
     * @return Active effect label
     */
    String getActiveEffectName();

    /**
     * Returns the enum type of the active status effect.
     *
     * @return Active PlayerEffect
     */
    PlayerEffect getActiveEffect();

    /**
     * Returns the remaining normalized duration of the active status effect in [0.0, 1.0].
     *
     * @return Normalized effect progress
     */
    double getEffectProgress();

    /**
     * Returns a defensive copy of the player's head collision circle.
     *
     * @return Head Circle copy
     */
    Circle getHeadShape();

    /**
     * Returns a defensive copy of the player's torso collision rectangle.
     *
     * @return Body Rectangle copy
     */
    Rectangle getBodyShape();

    /**
     * Returns a defensive copy of the player's foot collision circle.
     *
     * @return Foot Circle copy
     */
    Circle getFootShape();

    /**
     * Returns the current upgrade level for the specified player attribute.
     *
     * @param type Attribute to query
     * @return Level between 1 and 10
     */
    int getStat(StatType type);

    /**
     * Returns the accumulated reward points belonging to this player's profile.
     *
     * @return Accumulated points
     */
    int getPoints();
}
