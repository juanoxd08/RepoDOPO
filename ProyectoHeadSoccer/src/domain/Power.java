package domain;

/**
 * Base abstract class defining the rules and mechanics of special power shots according to DOPOSoccer specs:
 * 1. Charges in 10 seconds (~600 ticks at 60 FPS).
 * 2. Gains 25% charge instantly upon receiving a goal.
 * 3. Activating power gives the player an enduring visible aura and immediately begins recharging.
 * 4. Hitting the ball releases the power shot, which vanishes the aura and accelerates the ball.
 * 5. Hitting the opponent applies damage: DaÃ±oPoder = DaÃ±oTiro * multiplicadorKick, and applies the specific status effect.
 * 
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public abstract class Power {
    protected String name;
    protected double charge;
    protected boolean auraActive;
    protected boolean powerShotActive;
    protected double baseDamage;
    protected double effectDurationSeconds;
    protected PlayerEffect effectType;

    /**
     * Constructs a special power with specific attributes.
     * 
     * @param name Name of the power shot
     * @param baseDamage Base power damage (DaÃ±oTiro from Table 1)
     * @param effectDurationSeconds Duration in seconds of status effect
     * @param effectType Status effect applied to opponent upon impact
     */
    public Power(String name, double baseDamage, double effectDurationSeconds, PlayerEffect effectType) {
        this.name = name;
        this.baseDamage = baseDamage;
        this.effectDurationSeconds = effectDurationSeconds;
        this.effectType = effectType;
        this.charge = 0.0;
        this.auraActive = false;
        this.powerShotActive = false;
    }

    /**
     * Advances power charge per frame (fills 100% in 10 seconds = 600 ticks at 60 FPS).
     */
    public void tickCharge() {
        if (charge < 100.0) {
            charge = Math.min(100.0, charge + (100.0 / 600.0));
        }
    }

    /**
     * Increases power gauge by 25% (1/4) upon receiving an opponent goal.
     */
    public void addQuarterOnGoalReceived() {
        this.charge = Math.min(100.0, this.charge + 25.0);
    }

    /**
     * Checks if power gauge is at 100% and ready to activate.
     * 
     * @return True if charged to 100%
     */
    public boolean isReady() {
        return charge >= 100.0;
    }

    /**
     * Activates player aura when gauge is full, draining the bar to immediately start recharging.
     * 
     * @return True if successfully activated, false otherwise
     */
    public boolean activateAura() {
        if (isReady() && !auraActive && !powerShotActive) {
            this.auraActive = true;
            this.charge = 0.0;
            return true;
        }
        return false;
    }

    /**
     * Executes the power shot when the player with active aura contacts the soccer ball.
     *
     * @param ball Soccer ball in play
     * @param side Side of the shooting player
     */
    public void executePowerShot(Ball ball, Side side) {
        if (auraActive && ball != null) {
            this.auraActive = false;
            this.powerShotActive = true;
            ball.setPowerShot(true);
            affectBall(ball, side);
        }
    }

    /**
     * Computes the damage and status effect payload when a power shot hits the opponent:
     * PowerDamage = BaseDamage * kickMultiplier.
     *
     * @param kickMultiplier Shooter's kick damage multiplier
     * @return PowerHit payload, or null if no power shot is active
     */
    public PowerHit onHitRival(double kickMultiplier) {
        if(!powerShotActive) return null;
        this.powerShotActive = false;
            return new PowerHit(kickMultiplier * baseDamage, effectType, effectDurationSeconds);
        
    }

    /**
     * Resets shot state if ball enters goal or effect expires without hitting rival.
     */
    public void finishPowerShot() {
        this.powerShotActive = false;
    }

    /**
     * Optional per-frame update while a power shot is active (e.g., illusion clones).
     *
     * @param ball Main ball in play
     */
    public void updateShot(Ball ball) {
    }

    /**
     * Resets power to empty uncharged state.
     */
    public void reset() {
        this.charge = 0.0;
        this.auraActive = false;
        this.powerShotActive = false;
    }

    public String getName() { return name; }
    public double getCharge() { return charge; }
    public void setCharge(double charge) { this.charge = Math.max(0.0, Math.min(100.0, charge)); }
    public boolean isAuraActive() { return auraActive; }
    public void setAuraActive(boolean auraActive) { this.auraActive = auraActive; }
    public boolean isPowerShotActive() { return powerShotActive; }
    public void setPowerShotActive(boolean active) { this.powerShotActive = active; }
    public double getBaseDamage() { return baseDamage; }
    public double getEffectDurationSeconds() { return effectDurationSeconds; }
    public PlayerEffect getEffectType() { return effectType; }

    /**
     * Imparts characteristic velocity and trajectory onto the ball according to the country's power.
     * 
     * @param ball Ball in play
     * @param side Side of the shooter
     */
    public abstract void affectBall(Ball ball, Side side);
}
