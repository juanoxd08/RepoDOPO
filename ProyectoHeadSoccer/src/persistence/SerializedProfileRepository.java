package persistence;

import domain.DOPOSoccerException;
import domain.PlayerProfile;
import domain.ProfileRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Persistence implementation of {@link ProfileRepository} storing player profiles
 * in a serialized map file.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class SerializedProfileRepository implements ProfileRepository {
    private final File storageFile;

    /**
     * Constructs a profile repository backed by the specified storage file.
     *
     * @param storageFile File where serialized profiles are stored (defaults to profiles.dat if null)
     */
    public SerializedProfileRepository(File storageFile) {
        this.storageFile = (storageFile != null) ? storageFile : new File("profiles.dat");
    }

    /**
     * Constructs a profile repository backed by the default {@code profiles.dat} file.
     */
    public SerializedProfileRepository() {
        this(new File("profiles.dat"));
    }

    /**
     * Loads the profile for the given player name, or returns a fresh level-1 profile if absent.
     *
     * @param name Player display name
     * @return Loaded or newly initialized PlayerProfile
     * @throws DOPOSoccerException if the name is null/blank or reading fails
     */
    @Override
    public PlayerProfile load(String name) throws DOPOSoccerException {
        if (name == null || name.isBlank()) {
            throw new DOPOSoccerException(DOPOSoccerException.NULL_ARGUMENT);
        }
        Map<String, PlayerProfile> profiles = loadAll();
        return profiles.getOrDefault(name, new PlayerProfile(name));
    }

    /**
     * Saves or updates the given player profile in the repository file.
     *
     * @param profile Player profile to persist
     * @throws DOPOSoccerException if the profile is null or writing fails
     */
    @Override
    public void save(PlayerProfile profile) throws DOPOSoccerException {
        if (profile == null) {
            throw new DOPOSoccerException(DOPOSoccerException.NULL_ARGUMENT);
        }
        Map<String, PlayerProfile> profiles = loadAll();
        profiles.put(profile.getName(), profile);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(storageFile))) {
            out.writeObject(profiles);
        } catch (IOException e) {
            Log.record(e);
            throw new DOPOSoccerException(DOPOSoccerException.SAVE_ERROR);
        }
    }

    /*
     * Reads all stored profiles from disk, returning an empty map if the file does not exist yet.
     */
    @SuppressWarnings("unchecked")
    private Map<String, PlayerProfile> loadAll() throws DOPOSoccerException {
        if (!storageFile.exists()) {
            return new HashMap<>();
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(storageFile))) {
            return (Map<String, PlayerProfile>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            Log.record(e);
            throw new DOPOSoccerException(DOPOSoccerException.OPEN_ERROR);
        }
    }
}