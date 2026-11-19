package domain;

import java.io.Serializable;

/**
 * Persistent player profile holding identity, upgradeable stats, and accumulated points.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class PlayerProfile implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final PlayerStats stats;
    private int points;

    /**
     * Creates a player profile with explicit stats.
     *
     * @param name Player display name
     * @param stats Player statistics (defaults to level 1 if null)
     */
    public PlayerProfile(String name, PlayerStats stats) {
        this.name = (name != null) ? name : "Player";
        this.stats = (stats != null) ? stats : new PlayerStats();
        this.points = 0;
    }

    /**
     * Creates a player profile with baseline statistics.
     *
     * @param name Player display name
     */
    public PlayerProfile(String name) {
        this(name, new PlayerStats());
    }

    /** @return Player display name */
    public String getName() {
        return name;
    }

    /** @return Player statistics */
    public PlayerStats getStats() {
        return stats;
    }

    /**
     * Returns the level of a specific statistic.
     *
     * @param type Statistic type
     * @return Statistic level (1 to 10)
     */
    public int getStat(StatType type) {
        if (type == null) return 1;
        switch (type) {
            case SPEED: return stats.getSpeed();
            case JUMP: return stats.getJump();
            case KICK: return stats.getKick();
            case SURVIVAL: return stats.getSurvival();
            default: return 1;
        }
    }

    /** @return Accumulated reward points */
    public int getPoints() {
        return points;
    }

    /**
     * Adds reward points to this profile.
     *
     * @param amount Points to add
     */
    public void addPoints(int amount) {
        if (amount > 0) {
            this.points += amount;
        }
    }
}
