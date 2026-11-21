package domain;

import java.util.function.Supplier;

/**
 * Enumerates temporary status effects applied to players by power shots or exhaustion.
 * 
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public enum PlayerEffect {

    NONE(NoneEffect::new),
    PUSHED(PushedEffect::new),
    ELECTROCUTED(ElectrocutedEffect::new),
    KNOCKED_OUT(KnockedOutEffect::new),
    CONFUSED(ConfusedEffect::new);
    

    private final Supplier<Effect> effectFactory;

    PlayerEffect(Supplier<Effect> effectFactory){
        this.effectFactory = effectFactory;
    }

    /**
     * Creates a new strategy instance for this effect type.
     *
     * @return Effect strategy instance
     */
    public Effect createEffect(){
        return effectFactory.get();
    }

}
