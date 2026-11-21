package domain;

import java.io.Serializable;

/**
 * Persistent player profile holding identity, upgradeable stats, and accumulated points.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class PlayerProfile implements Serializable, ProfileView {
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
     * Returns the point cost required to upgrade the given statistic by one level,
     * or 0 if null or already at maximum level.
     *
     * @param type Statistic type
     * @return Upgrade cost in points
     */
    @Override 
    public int upgradeCost(StatType type){
        if(type == null) return 0;
        int level = getStat(type);
        if(level >= 10) return 0;
        return 500 * (1 << (level - 1));
    }

    /**
     * Upgrades the specified statistic by one level, deducting the required points.
     *
     * @param type Statistic type to upgrade
     * @throws DOPOSoccerException if type is null, max level is reached, or points are insufficient
     */
    public void upgrade(StatType type) throws DOPOSoccerException {
        if (type == null) {
            throw new DOPOSoccerException(DOPOSoccerException.NULL_ARGUMENT);
        }
        if (getStat(type) >= 10) {
            throw new DOPOSoccerException(DOPOSoccerException.MAX_STAT_REACHED);
        }
        int cost = upgradeCost(type);
        if (points < cost) {
            throw new DOPOSoccerException(DOPOSoccerException.NOT_ENOUGH_POINTS);
        }
        points -= cost;
        stats.increment(type);
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
