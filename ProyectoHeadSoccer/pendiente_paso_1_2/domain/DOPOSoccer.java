package domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Main Facade for the DOPOSoccer domain layer.
 */
public class DOPOSoccer {
    private Match currentMatch;
    private final List<MatchListener> listeners = new ArrayList<>();

    public void newMatch(MatchConfig config) throws DOPOSoccerException {
        if (config == null) {
            throw new DOPOSoccerException(DOPOSoccerException.NULL_ARGUMENT);
        }
        this.currentMatch = new Match(config);
        for (MatchListener listener : listeners) {
            this.currentMatch.addListener(listener);
        }
    }

    public void tick() {
        if (currentMatch != null) {
            currentMatch.tick();
        }
    }

    public void setIntention(Side side, Set<PlayerAction> actions) {
        if (currentMatch != null && side != null) {
            currentMatch.setIntention(side, actions);
        }
    }

    public void pause() {
        if (currentMatch != null) {
            currentMatch.pause();
        }
    }

    public void resume() {
        if (currentMatch != null) {
            currentMatch.resume();
        }
    }

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

    public void finish() {
        if (currentMatch != null) {
            currentMatch.finish();
        }
    }

    public MatchView getMatch() {
        return currentMatch;
    }

    public void addListener(MatchListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            if (currentMatch != null) {
                currentMatch.addListener(listener);
            }
        }
    }

    public void removeListener(MatchListener listener) {
        listeners.remove(listener);
        if (currentMatch != null) {
            currentMatch.removeListener(listener);
        }
    }
}