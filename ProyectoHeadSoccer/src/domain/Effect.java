package domain;

import java.io.Serializable;

/**
 * Strategy interface defining movement restrictions and per-tick behavior
 * for status effects applied to a player.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public interface Effect extends Serializable {

	/**
	 * Returns the effect enumeration type for view and HUD reporting.
	 *
	 * @return Effect type
	 */
	PlayerEffect getType();

	/**
	 * Returns the horizontal movement speed multiplier.
	 *
	 * @return Speed multiplier (1.0 by default)
	 */
	default double speedFactor() {
		return 1.0;
	}

	/**
	 * Checks whether the player is allowed to move horizontally.
	 *
	 * @return True if horizontal movement is allowed
	 */
	default boolean canMove() {
		return true;
	}

	/**
	 * Checks whether the player is allowed to jump.
	 *
	 * @return True if jumping is allowed
	 */
	default boolean canJump() {
		return true;
	}

	/**
	 * Checks whether the player is allowed to kick or activate powers.
	 *
	 * @return True if kicking is allowed
	 */
	default boolean canKick() {
		return true;
	}

	/**
	 * Checks whether left and right movement controls are inverted.
	 *
	 * @return True if controls are inverted
	 */
	default boolean isConfused() {
		return false;
	}

	/**
	 * Executes one-time physical setup when the effect is first applied.
	 *
	 * @param body Player body receiving the effect
	 */
	default void onStart(PlayerBody body) {
	}

	/**
	 * Executes per-frame physical behavior while the effect remains active.
	 *
	 * @param body Player body affected
	 * @param side Pitch side of the affected player
	 */
	default void onTick(PlayerBody body, Side side) {
	}
}
