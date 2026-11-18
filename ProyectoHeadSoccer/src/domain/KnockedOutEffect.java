package domain;

/**
 * Effect strategy applied by TornadoPower: launches the player upward and disables all actions.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class KnockedOutEffect implements Effect {

	/**
	 * Returns the KNOCKED_OUT effect type.
	 *
	 * @return PlayerEffect.KNOCKED_OUT
	 */
	@Override
	public PlayerEffect getType() {
		return PlayerEffect.KNOCKED_OUT;
	}

	/**
	 * Disables horizontal movement while knocked out.
	 *
	 * @return false
	 */
	@Override
	public boolean canMove() {
		return false;
	}

	/**
	 * Disables jumping while knocked out.
	 *
	 * @return false
	 */
	@Override
	public boolean canJump() {
		return false;
	}

	/**
	 * Disables kicking and power activation while knocked out.
	 *
	 * @return false
	 */
	@Override
	public boolean canKick() {
		return false;
	}

	/**
	 * Launches the player upward and clears ground support on impact.
	 *
	 * @param body Player body receiving the effect
	 */
	@Override
	public void onStart(PlayerBody body) {
		if (body != null) {
			body.setVerticalSpeed(-10.0);
			body.clearSupport();
		}
	}
}
