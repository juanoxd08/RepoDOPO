package domain;

/**
 * Tracks the active power status effect on a player and its movement/action restrictions.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class EffectState {
    private Effect currentEffect;
    private int remainingTicks;

    /**
     * Initializes an empty effect state with no active restriction.
     */
    public EffectState(){
        this.currentEffect = new NoneEffect();
        this.remainingTicks = 0;
    }

    /**
     * Applies a status effect, replacing any previous effect immediately.
     *
     * @param effect Status effect strategy to apply
     * @param duration Duration in seconds
     * @param body Player body receiving the effect
     */
    public void apply(Effect effect, double duration, PlayerBody body){
        this.currentEffect = (effect != null) ? effect : new NoneEffect();
        this.remainingTicks = Physics.seconds(duration);
        this.currentEffect.onStart(body);
    }

    /**
     * Advances the active effect timer and clears it upon expiration.
     *
     * @param body Player body affected
     * @param side Pitch side of the affected player
     */
    public void tick(PlayerBody body, Side side){
        if(this.currentEffect.getType() != PlayerEffect.NONE && remainingTicks > 0){
            this.currentEffect.onTick(body, side);
            remainingTicks--;
            if(remainingTicks <= 0){
                clear();
            }
        }
    }

    /**
     * Clears any active status effect immediately.
     */
    public void clear(){
        this.currentEffect = new NoneEffect();
        this.remainingTicks = 0;
    }

    /** @return Movement speed multiplier */
    public double speedFactor(){
        return currentEffect.speedFactor();
    }

    /** @return True if the player can move horizontally */
    public boolean canMove(){
        return currentEffect.canMove();
    }

    /** @return True if the player can jump */
    public boolean canJump(){
        return currentEffect.canJump();
    }

    /** @return True if the player can kick or activate power */
    public boolean canKick(){
        return currentEffect.canKick();
    }

    /** @return True if horizontal controls are inverted */
    public boolean isConfused(){
        return currentEffect.isConfused();
    }

    /** @return Active status effect type */
    public PlayerEffect getCurrent(){
        return currentEffect.getType();
    }

    /** @return Remaining effect duration in ticks */
    public int getTicksRemaining(){
        return remainingTicks;
    }
}
