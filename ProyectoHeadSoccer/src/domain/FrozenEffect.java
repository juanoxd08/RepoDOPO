package domain;

/**
 * Effect strategy applied by FreezePower: immobilizes the player and marks the head as stunned.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class FrozenEffect implements Effect {

	/**
	 * Returns the FROZEN effect type.
	 *
	 * @return PlayerEffect.FROZEN
	 */
	@Override
	public PlayerEffect getType() {
		return PlayerEffect.FROZEN;
	}

	/**
	 * Disables horizontal movement while frozen.
	 *
	 * @return false
	 */
	@Override
	public boolean canMove() {
		return false;
	}

	/**
	 * Disables jumping while frozen.
	 *
	 * @return false
	 */
	@Override
	public boolean canJump() {
		return false;
	}

	/**
	 * Disables kicking and power activation while frozen.
	 *
	 * @return false
	 */
	@Override
	public boolean canKick() {
		return false;
	}

	/**
	 * Marks the player's head as stunned when frozen.
	 *
	 * @param body Player body receiving the effect
	 */
	@Override
	public void onStart(PlayerBody body) {
		if (body != null) {
			body.getHead().setStunned(true);
		}
	}
}
