package domain;

import java.io.Serializable;

/**
 * Manages player health, zero-health stun state, and 3-second recovery.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class Life implements Serializable {
    private static final long serialVersionUID = 1L;
    private final double MAX_LIFE;
    private double life;
    private boolean stunned;
    private int stunTicksRemaining;

    /**
     * Creates a life component initialized at full health.
     *
     * @param maxLife Maximum health points
     */
    public Life(double maxLife){
        this.MAX_LIFE = maxLife;
        this.life = maxLife;
        this.stunned = false;
        this.stunTicksRemaining = 0;
    }

    /**
     * Deducts damage and triggers a 3-second stun if health reaches zero.
     *
     * @param damage Damage amount to apply
     */
    public void takeDamage(double damage){
        if(stunned) return;
        this.life = Math.max(0.0, this.life - damage);
        if(this.life <= 0.0){
            stunTicksRemaining = Physics.seconds(3.0);
            stunned = true;
        }
    }

    /**
     * Advances stun timer and restores full health when stun expires.
     */
    public void tick(){
        if(stunned){
            stunTicksRemaining--;
            if(stunTicksRemaining <= 0){
                stunTicksRemaining = 0;
                stunned = false;
                life = MAX_LIFE;
            }
        }
    }

    /**
     * Restores full health and clears any active stun state.
     */
    public void reset(){
        this.life = MAX_LIFE;
        this.stunned = false;
        this.stunTicksRemaining = 0;
    }

    /** @return Current health points */
    public double getCurrent() {
        return life;
    }

    /** @return Maximum health points */
    public double getMax() {
        return MAX_LIFE;
    }

    /** @return True if stunned due to zero health */
    public boolean isStunned() {
        return stunned;
    }

    /** @return Remaining stun ticks */
    public int getStunTicksRemaining() {
        return stunTicksRemaining;
    }
}
