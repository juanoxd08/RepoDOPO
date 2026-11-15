package domain;

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
}