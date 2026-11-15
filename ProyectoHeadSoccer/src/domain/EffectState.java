package domain;

/**
 * Tracks the active power status effect on a player and its movement/action restrictions.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class EffectState {
    private PlayerEffect actualStatus;
    private int remainingTicks;

    /**
     * Initializes an empty effect state with no active restriction.
     */
    public EffectState(){
        this.actualStatus = PlayerEffect.NONE;
        this.remainingTicks = 0;
    }

    /**
     * Applies a status effect, replacing any previous effect immediately.
     *
     * @param effect Status effect to apply
     * @param duration Duration in seconds
     */
    public void apply(PlayerEffect effect, double duration){
        this.actualStatus = (effect != null) ? effect : PlayerEffect.NONE;
        this.remainingTicks = Physics.seconds(duration);
    }

    /**
     * Advances the active effect timer and clears it upon expiration.
     */
    public void tick(){
        if(actualStatus != PlayerEffect.NONE && remainingTicks > 0){
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
        this.actualStatus = PlayerEffect.NONE;
        this.remainingTicks = 0;
    }

    /** @return Movement speed multiplier (0.5 when electrocuted, 1.0 otherwise) */
    public double speedFactor(){
        return (actualStatus == PlayerEffect.ELECTROCUTED) ? 0.5 : 1.0;
    }

    /** @return True if the player can move horizontally */
    public boolean canMove(){
        return actualStatus != PlayerEffect.FROZEN && actualStatus != PlayerEffect.KNOCKED_OUT;
    }

    /** @return True if the player can jump */
    public boolean canJump(){
        return canMove() && actualStatus != PlayerEffect.ELECTROCUTED;
    }

    /** @return True if the player can kick or activate power */
    public boolean canKick(){
        return canMove();
    }

    /** @return True if horizontal controls are inverted */
    public boolean isConfused(){
        return actualStatus == PlayerEffect.CONFUSED;
    }

    /** @return Active status effect */
    public PlayerEffect getCurrent(){
        return actualStatus;
    }

    /** @return Remaining effect duration in ticks */
    public int getTicksRemaining(){
        return remainingTicks;
    }
}
