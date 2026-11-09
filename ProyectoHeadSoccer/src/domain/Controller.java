package domain;

import java.util.Set;

public interface Controller {
    public Set<PlayerAction> decide(ReaderMatch match);
    public default void receive(Set<PlayerAction> intention){
        
    }
}
