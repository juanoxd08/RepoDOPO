package domain;

import java.io.File;

/**
 * Repository contract for persisting and loading match sessions.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public interface MatchRepository {
    /**
     * Saves the given match to a file.
     *
     * @param match Match instance to persist
     * @param file Destination file
     * @throws DOPOSoccerException if saving fails
     */
    void save(Match match, File file) throws DOPOSoccerException;

    /**
     * Opens and restores a saved match from a file.
     *
     * @param file Source file
     * @return Restored match instance
     * @throws DOPOSoccerException if loading fails
     */
    Match open(File file) throws DOPOSoccerException;
}