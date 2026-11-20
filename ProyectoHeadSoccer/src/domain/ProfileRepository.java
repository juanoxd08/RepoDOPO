package domain;

/**
 * Repository contract for persisting and loading player profiles across matches.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public interface ProfileRepository {

    /**
     * Loads the profile associated with the given player name, or creates a default one if not found.
     *
     * @param name Player display name
     * @return Loaded or newly created PlayerProfile
     * @throws DOPOSoccerException if loading fails or name is invalid
     */
    PlayerProfile load(String name) throws DOPOSoccerException;

    /**
     * Persists the given player profile.
     *
     * @param profile Player profile to save
     * @throws DOPOSoccerException if saving fails or profile is null
     */
    void save(PlayerProfile profile) throws DOPOSoccerException;
}