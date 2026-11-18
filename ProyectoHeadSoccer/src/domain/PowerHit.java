package domain;

/**
 * Value object describing the damage and status effect produced by a power shot hit.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class PowerHit {
    private final double damage;
    private final Effect effect;
    private final double durationSeconds;

    /**
     * Creates a power hit outcome.
     *
     * @param damage Health damage to inflict
     * @param effect Status effect strategy to apply (or NoneEffect)
     * @param durationSeconds Duration of the status effect in seconds
     */
    public PowerHit(double damage, Effect effect, double durationSeconds) {
        this.damage = damage;
        this.effect = (effect != null) ? effect : new NoneEffect();
        this.durationSeconds = durationSeconds;
    }

    /** @return Health damage inflicted */
    public double getDamage() {
        return damage;
    }

    /** @return Status effect applied */
    public Effect getEffect() {
        return effect;
    }

    /** @return Effect duration in seconds */
    public double getDurationSeconds() {
        return durationSeconds;
    }
}
