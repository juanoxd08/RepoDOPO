package domain;

/**
 * Effect strategy applied by AuraAzulPower: pushes the opponent toward their own goal each tick.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class PushedEffect implements Effect{
    /**
     * Returns the PUSHED effect type.
     *
     * @return PlayerEffect.PUSHED
     */
    @Override 
    public PlayerEffect getType(){
        return PlayerEffect.PUSHED;
    }

    /**
     * Translates the player toward their own goal and clamps to pitch boundaries.
     *
     * @param body Player body affected
     * @param side Pitch side of the affected player
     */
    @Override
    public void onTick(PlayerBody body, Side side){
        if(body == null || side == null) return;
        body.translate(-4.0* side.dir(), 0.0);
        body.collideWithEdges();
    }
}
