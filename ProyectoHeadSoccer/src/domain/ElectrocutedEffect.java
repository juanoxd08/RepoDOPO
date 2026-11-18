package domain;

/**
 * Effect strategy applied by LightningPower: halves movement speed and disables jumping.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class ElectrocutedEffect implements Effect {

	/**
	 * Returns the ELECTROCUTED effect type.
	 *
	 * @return PlayerEffect.ELECTROCUTED
	 */
	@Override
	public PlayerEffect getType() {
		return PlayerEffect.ELECTROCUTED;
	}

	/**
	 * Halves horizontal movement speed while electrocuted.
	 *
	 * @return 0.5 speed factor
	 */
	@Override
	public double speedFactor() {
		return 0.5;
	}

	/**
	 * Disables jumping while electrocuted.
	 *
	 * @return false
	 */
	@Override
	public boolean canJump() {
		return false;
	}
}
