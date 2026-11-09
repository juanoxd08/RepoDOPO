package domain;

/**
 * Abstract base class representing a player's special power ability, managing charging logic and activation.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public abstract class Power {
    protected int charge;
    protected boolean active;
    protected int chargeTicks;

    /**
     * Constructs a power initialized with 100% full charge ready for activation.
     */
    public Power() {
        this.charge = 100;
        this.active = false;
        this.chargeTicks = 0;
    }

    /**
     * Returns the current charge percentage (0 to 100).
     * 
     * @return Current charge level
     */
    public int getCharge() {
        return charge;
    }

    /**
     * Sets the charge percentage within valid bounds (0 to 100).
     * 
     * @param charge Charge level to assign
     */
    public void setCharge(int charge) {
        this.charge = Math.max(0, Math.min(100, charge));
    }

    /**
     * Advances the power charge timer by one frame tick.
     */
    public void tick() {
        if (charge < 100) {
            chargeTicks++;
            if (chargeTicks >= 3) {
                charge++;
                chargeTicks = 0;
            }
        }
    }

    /**
     * Checks whether the power bar is fully charged and ready for use.
     * 
     * @return True if charged to 100, false otherwise
     */
    public boolean isReady() {
        return charge >= 100;
    }

    /**
     * Activates the special power, resetting charge and imparting special ball velocity.
     * 
     * @param ball Soccer ball in play
     * @param side Pitch side of the activating player
     * @return True if activated successfully, false if not ready
     */
    public boolean activate(Ball ball, Side side) {
        if (!isReady()) {
            return false;
        }
        this.active = true;
        this.charge = 0;
        this.chargeTicks = 0;
        if (ball != null) {
            affectBall(ball, side);
        }
        return true;
    }

    /**
     * Resets the power state and discharges stored energy.
     */
    public void reset() {
        this.charge = 0;
        this.chargeTicks = 0;
        this.active = false;
    }

    /**
     * Indicates whether the special power effect is currently active in the match.
     * 
     * @return True if active, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the active state of the special power.
     * 
     * @param active True if active, false otherwise
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Applies the power effect upon contacting the opponent player.
     * 
     * @param ball Soccer ball in play
     * @param rival Opponent player
     */
    public abstract void applyPower(Ball ball, Player rival);

    /**
     * Ends the power effect applied to the opponent.
     * 
     * @param rival Opponent player
     */
    public abstract void stopPower(Player rival);

    /**
     * Applies special trajectory or impulse to the ball upon shooting.
     * 
     * @param ball Soccer ball in play
     * @param side Pitch side of the shooter
     */
    public abstract void affectBall(Ball ball, Side side);
}
