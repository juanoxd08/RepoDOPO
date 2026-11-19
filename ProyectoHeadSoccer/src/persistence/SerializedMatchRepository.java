package persistence;

import domain.DOPOSoccerException;
import domain.Match;
import domain.MatchRepository;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Persistence implementation of {@link MatchRepository} using standard Java object serialization.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class SerializedMatchRepository implements MatchRepository {

    /**
     * Serializes the given match state to the target file.
     *
     * @param match Match instance to persist
     * @param file Destination file
     * @throws DOPOSoccerException if arguments are null or writing fails
     */
    @Override
    public void save(Match match, File file) throws DOPOSoccerException {
        if (match == null || file == null) {
            throw new DOPOSoccerException(DOPOSoccerException.NULL_ARGUMENT);
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(match);
        } catch (IOException e) {
            Log.record(e);
            throw new DOPOSoccerException(DOPOSoccerException.SAVE_ERROR);
        }
    }

    /**
     * Deserializes a previously saved match from the source file.
     *
     * @param file Source file containing serialized match data
     * @return Restored match instance
     * @throws DOPOSoccerException if the file is null, unreadable, or incompatible
     */
    @Override
    public Match open(File file) throws DOPOSoccerException {
        if (file == null) {
            throw new DOPOSoccerException(DOPOSoccerException.NULL_ARGUMENT);
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (Match) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            Log.record(e);
            throw new DOPOSoccerException(DOPOSoccerException.OPEN_ERROR);
        }
    }
}
