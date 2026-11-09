package domain;

import java.util.HashSet;
import java.util.Set;

public class HumanController implements Controller {
    private Set<PlayerAction> actions = new HashSet<>();
    
    @Override 
    public Set<PlayerAction> decide(ReaderMatch match) {
        return this.actions;
    }

    @Override 
    public void receive(Set<PlayerAction> intention) {
        this.actions = (intention != null) ? intention : new HashSet<>();
    }
}
