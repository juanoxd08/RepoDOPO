package domain;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Base abstract class defining the rules and mechanics of special power shots according to DOPOSoccer specs:
 * 1. Charges in 10 seconds (~600 ticks at 60 FPS).
 * 2. Gains 25% charge instantly upon receiving a goal.
 * 3. Activating power gives the player an enduring visible aura and immediately begins recharging.
 * 4. Hitting the ball releases the power shot, which vanishes the aura and accelerates the ball.
 * 5. Hitting the opponent applies damage: powerDamage = baseDamage * kickMultiplier, and applies the specific status effect.
 * 
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public abstract class Power implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int TICKS_PER_POWER = 90;
    public static final int MAX_FLIGHT_TICKS = Physics.seconds(2);
    private static final double CHARGE_LIFT_PX = 30.0;

    private final String name;
    private double charge;
    private boolean auraActive;
    private boolean powerShotActive;
    private final double effectDurationSeconds;
    private int ticksPerPower;
    private Side shotSide;
    private double holdX;
    private double holdY;
    private int flightRemainingTicks;

    /**
     * Constructs a special power with its display name and effect duration.
     * 
     * @param name Name of the power shot
     * @param effectDurationSeconds Duration in seconds of status effect
     */
    public Power(String name, double effectDurationSeconds) {
        this.name = name;
        this.effectDurationSeconds = effectDurationSeconds;
        this.charge = 0.0;
        this.auraActive = false;
        this.powerShotActive = false;
        this.ticksPerPower = 0;
        this.holdX = 0;
        this.holdY = 0;
        this.shotSide = null;
    }

    /**
     * Advances power charge per frame (fills 100% in 10 seconds = 600 ticks at 60 FPS).
     */
    public final void tickCharge() {
        if (charge < 100.0) {
            charge = Math.min(100.0, charge + (100.0 / 600.0));
        }
    }

    /**
     * Increases power gauge by 25% (1/4) upon receiving an opponent goal.
     */
    public final void addQuarterOnGoalReceived() {
        this.charge = Math.min(100.0, this.charge + 25.0);
    }

    /**
     * Checks if power gauge is at 100% and ready to activate.
     * 
     * @return True if charged to 100%
     */
    public final boolean isReady() {
        return charge >= 100.0;
    }

    /**
     * Activates player aura when gauge is full, draining the bar to immediately start recharging.
     * 
     * @return True if successfully activated, false otherwise
     */
    public final boolean activateAura() {
        if (isReady() && !auraActive && !powerShotActive) {
            this.auraActive = true;
            this.charge = 0.0;
            return true;
        }
        return false;
    }

    /**
     * Executes the power shot when the player with active aura contacts the soccer ball,
     * suspending the ball above its current position.
     *
     * @param ball Soccer ball in play
     * @param side Side of the shooting player
     */
    public final void executePowerShot(Ball ball, Side side) {
        if (ball == null) return;
        double targetY = Math.max(ball.getRadius() + 5, ball.getY() - CHARGE_LIFT_PX);
        executePowerShot(ball, side, ball.getX(), targetY);
    }

    /**
     * Executes the power shot and suspends the ball at the specified charging coordinates
     * (clamped inside field boundaries) before launching toward the opponent's goal.
     *
     * @param ball Soccer ball in play
     * @param side Side of the shooting player
     * @param targetX Desired horizontal charging coordinate
     * @param targetY Desired vertical charging coordinate
     */
    public final void executePowerShot(Ball ball, Side side, double targetX, double targetY) {
        if (auraActive && ball != null && !ball.isPowerShotCharging()) {
            this.auraActive = false;
            this.powerShotActive = true;
            ball.setPowerShot(true);
            ball.setPowerShotCharging(true, 0.0);
            double r = ball.getRadius();
            holdX = Math.max(r, Math.min(Field.WIDTH - r, targetX));
            holdY = Math.max(r + 5, Math.min(Field.HEIGHT - r, targetY));
            ball.getCircle().setX(holdX);
            ball.getCircle().setY(holdY);
            shotSide = side;
            ticksPerPower = TICKS_PER_POWER;
            ball.setHorizontalSpeed(0);
            ball.setVerticalSpeed(0);
        }
    }

    /**
     * Computes the damage and status effect payload when a power shot hits the opponent:
     * PowerDamage = BaseDamage * kickMultiplier.
     *
     * @param kickMultiplier Shooter's kick damage multiplier
     * @return PowerHit payload, or null if no power shot is active
     */
    public final PowerHit onHitRival(double kickMultiplier) {
        if (!powerShotActive || ticksPerPower > 0) return null;
        this.powerShotActive = false;
        onShotFinished();
        return new PowerHit(kickMultiplier * getBaseDamage(), createEffect(), effectDurationSeconds);
    }

    /**
     * Resets shot state if ball enters goal or effect expires without hitting rival.
     */
    public final void finishPowerShot() {
        this.flightRemainingTicks = 0;
        this.powerShotActive = false;
        this.ticksPerPower = 0;
        this.shotSide = null;
        onShotFinished();
    }

    /**
     * Advances power shot windup suspension and per-frame active shot behavior.
     *
     * @param ball Main ball in play
     */
    public final void updateShot(Ball ball) {
        if (ball == null) return;
        if (ticksPerPower > 0) {
            ticksPerPower--;
            ball.getCircle().setX(holdX);
            ball.getCircle().setY(holdY);
            ball.setHorizontalSpeed(0);
            ball.setVerticalSpeed(0);
            ball.setPowerShotCharging(ticksPerPower > 0, 1.0 - ((double) ticksPerPower/TICKS_PER_POWER));
            if (ticksPerPower == 0 && shotSide != null) {
                shapeShot(ball, shotSide);
                flightRemainingTicks = MAX_FLIGHT_TICKS;
            }
        } else if (powerShotActive) {
            flightRemainingTicks--;
            updateActiveShot(ball);
            if(flightRemainingTicks <= 0){
                finishPowerShot();
                ball.setPowerShot(false);
            }
        }
    }

    /**
     * Optional per-frame hook while a launched power shot is active (e.g., illusion clones).
     *
     * @param ball Main ball in play
     */
    protected void updateActiveShot(Ball ball) {
    }

    /**
     * Optional hook invoked when an active power shot terminates on hit, goal, or flight expiry.
     */
    protected void onShotFinished() {
    }

    /**
     * Resets power to empty uncharged state.
     */
    public final void reset() {
        this.flightRemainingTicks = 0;
        this.charge = 0.0;
        this.auraActive = false;
        this.powerShotActive = false;
        this.ticksPerPower = 0;
        this.shotSide = null;
        onShotFinished();
    }

    /**
     * Imparts characteristic velocity and trajectory onto the ball according to the country's power.
     *
     * @param ball Ball in play
     * @param side Side of the shooter
     */
    protected abstract void shapeShot(Ball ball, Side side);

    /**
     * Returns the base power damage before kick multiplier scaling.
     *
     * @return Base damage value
     */
    public abstract double getBaseDamage();

    /**
     * Creates the status effect strategy applied to the opponent upon impact.
     *
     * @return Effect strategy instance
     */
    public abstract Effect createEffect();

    /**
     * Returns the display name of this power.
     *
     * @return Power name
     */
    public String getName() { return name; }

    /**
     * Returns the current charge level in [0.0, 100.0].
     *
     * @return Charge percentage
     */
    public double getCharge() { return charge; }

    /**
     * Sets the power charge clamped to [0.0, 100.0].
     *
     * @param charge New charge value
     */
    public void setCharge(double charge) { this.charge = Math.max(0.0, Math.min(100.0, charge)); }

    /**
     * Indicates whether the player's power aura is currently active.
     *
     * @return True if aura is active
     */
    public boolean isAuraActive() { return auraActive; }

    /**
     * Indicates whether this power shot is currently in its suspended windup charging phase.
     *
     * @return True while charging ticks remain
     */
    public boolean isChargingShot() { return ticksPerPower > 0; }

    /**
     * Indicates whether a power shot launched by this power is currently in flight.
     *
     * @return True if power shot is active
     */
    public boolean isPowerShotActive() { return powerShotActive; }

    /**
     * Returns the duration of the status effect inflicted by this power, in seconds.
     *
     * @return Effect duration in seconds
     */
    public double getEffectDurationSeconds() { return effectDurationSeconds; }

    /**
     * Decoy balls created by this power, if any. Only the Illusion power creates them.
     *
     * @return Copies of the decoy balls (empty by default)
     */
    public List<Circle> getDecoyBalls() {
        return Collections.emptyList();
    }
}
