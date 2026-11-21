package domain;

/**
 * Read-only view of a player's persistent profile, exposing identity,
 * accumulated points, stat levels, and upgrade costs without allowing direct mutation.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public interface ProfileView {

	/**
	 * Returns the player's display name.
	 *
	 * @return Player name
	 */
	String getName();

	/**
	 * Returns the player's accumulated reward points.
	 *
	 * @return Reward points
	 */
	int getPoints();

	/**
	 * Returns the current level of the given statistic.
	 *
	 * @param type Statistic type
	 * @return Statistic level (1 to 10)
	 */
	int getStat(StatType type);

	/**
	 * Returns the point cost required to upgrade the given statistic by one level,
	 * or 0 if the statistic is null or already at maximum level.
	 *
	 * @param type Statistic type
	 * @return Upgrade cost in points
	 */
	int upgradeCost(StatType type);
}
