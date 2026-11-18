package domain;

/**
 * Default no-op effect strategy when no status effect is active.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class NoneEffect implements Effect{
    
    /**
     * Returns the NONE effect type.
     *
     * @return PlayerEffect.NONE
     */
    @Override 
    public PlayerEffect getType(){
        return PlayerEffect.NONE;
    }
}
