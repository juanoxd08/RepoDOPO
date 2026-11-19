package domain;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Main Facade for the DOPOSoccer domain layer.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class DOPOSoccer {
    private Match currentMatch;
    private final List<MatchListener> listeners = new ArrayList<>();
    private final MatchRepository matchRepository;

    /**
     * Creates a facade instance without a configured match repository.
     */
    public DOPOSoccer() {
        this(null);
    }

    /**
     * Creates a facade instance with the given match persistence repository.
     *
     * @param matchRepository Repository used to save and open matches
     */
    public DOPOSoccer(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    /**
     * Starts a new match from the given configuration and attaches registered listeners.
     *
     * @param config Validated match configuration
     * @throws DOPOSoccerException if config is null or invalid
     */
    public void newMatch(MatchConfig config) throws DOPOSoccerException {
        if (config == null) {
            throw new DOPOSoccerException(DOPOSoccerException.NULL_ARGUMENT);
        }
        this.currentMatch = new Match(config);
        for (MatchListener listener : listeners) {
            this.currentMatch.addListener(listener);
        }
    }

    /**
     * Advances the active match simulation by one tick.
     */
    public void tick() {
        if (currentMatch != null) {
            currentMatch.tick();
        }
    }

    /**
     * Records player input intentions for the given side.
     *
     * @param side Player side
     * @param actions Set of actions pressed on the current frame
     */
    public void setIntention(Side side, Set<PlayerAction> actions) {
        if (currentMatch != null && side != null) {
            currentMatch.setIntention(side, actions);
        }
    }

    /**
     * Pauses the active match.
     */
    public void pause() {
        if (currentMatch != null) {
            currentMatch.pause();
        }
    }

    /**
     * Resumes the active match from pause.
     */
    public void resume() {
        if (currentMatch != null) {
            currentMatch.resume();
        }
    }

    /**
     * Toggles the active match between paused and playing states.
     */
    public void togglePause() {
        if (currentMatch == null) {
            return;
        }
        if (currentMatch.getStatus() == GameStatus.PAUSED) {
            currentMatch.resume();
        } else {
            currentMatch.pause();
        }
    }

    /**
     * Finishes the active match immediately.
     */
    public void finish() {
        if (currentMatch != null) {
            currentMatch.finish();
        }
    }

    /**
     * Returns a read-only view of the active match, or null if none exists.
     *
     * @return MatchView of the active match
     */
    public MatchView getMatch() {
        return currentMatch;
    }

    /**
     * Registers a match lifecycle listener.
     *
     * @param listener Listener to register
     */
    public void addListener(MatchListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            if (currentMatch != null) {
                currentMatch.addListener(listener);
            }
        }
    }

    /**
     * Unregisters a match lifecycle listener.
     *
     * @param listener Listener to remove
     */
    public void removeListener(MatchListener listener) {
        listeners.remove(listener);
        if (currentMatch != null) {
            currentMatch.removeListener(listener);
        }
    }

    /**
     * Saves the active match to the specified file.
     *
     * @param file Destination file
     * @throws DOPOSoccerException if there is no active match, repository is missing, or saving fails
     */
    public void save(File file) throws DOPOSoccerException {
        if (currentMatch == null) throw new DOPOSoccerException(DOPOSoccerException.INVALID_STATE);
        if (matchRepository == null) throw new DOPOSoccerException(DOPOSoccerException.NULL_ARGUMENT);
        matchRepository.save(currentMatch, file);
    }

    /**
     * Opens a saved match from the specified file and reattaches registered listeners.
     *
     * @param file Source file
     * @throws DOPOSoccerException if repository is missing or loading fails
     */
    public void open(File file) throws DOPOSoccerException {
        if (matchRepository == null) throw new DOPOSoccerException(DOPOSoccerException.NULL_ARGUMENT);
        currentMatch = matchRepository.open(file);

        if (currentMatch != null) {
            for (MatchListener listener : listeners) {
                this.currentMatch.addListener(listener);
            }
        }
    }
}