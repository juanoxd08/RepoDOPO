package domain;

/**
 * Effect strategy applied by IllusionPower: inverts horizontal movement controls.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class ConfusedEffect implements Effect {

	/**
	 * Returns the CONFUSED effect type.
	 *
	 * @return PlayerEffect.CONFUSED
	 */
	@Override
	public PlayerEffect getType() {
		return PlayerEffect.CONFUSED;
	}

	/**
	 * Inverts left and right movement controls while confused.
	 *
	 * @return true
	 */
	@Override
	public boolean isConfused() {
		return true;
	}
}
