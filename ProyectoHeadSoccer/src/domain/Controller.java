package domain;

import java.util.Set;

/**
 * Common interface for player input controllers, decoupled from presentation.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public interface Controller {

    /**
     * Decides the set of actions to execute during the current frame.
     * 
     * @param match Read-only match state for decision making
     * @return Set of decided actions
     */
    Set<PlayerAction> decide(ReaderMatch match);

    /**
     * Polymorphically receives raw input intentions from the user interface.
     * Default implementation ignores input (used by autonomous controllers).
     * 
     * @param intention Set of player actions intended by GUI
     */
    default void receive(Set<PlayerAction> intention) {
    }
}
