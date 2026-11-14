package domain;

import java.util.HashSet;
import java.util.Set;

/**
 * Controller implementation that manages player actions driven by human keyboard input.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class HumanController implements Controller {
    private Set<PlayerAction> actions = new HashSet<>();
    
    /**
     * Decides player actions by returning the currently buffered keyboard actions.
     * 
     * @param match Read-only perspective of the match state
     * @return Set of active player actions
     */
    @Override 
    public Set<PlayerAction> decide(ReaderMatch match) {
        return this.actions;
    }

    /**
     * Receives and buffers human input intentions from the presentation layer.
     * 
     * @param intention Set of player actions triggered by key inputs
     */
    @Override 
    public void receive(Set<PlayerAction> intention) {
        this.actions = (intention != null) ? intention : new HashSet<>();
    }
}
