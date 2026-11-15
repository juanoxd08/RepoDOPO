package domain;

import java.util.function.Supplier;

/**
 * Available playable countries, each providing a display name, power name, and power factory.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public enum Country {
	SOUTH_KOREA("Corea del Sur", "Aura Azul", AuraAzulPower::new ),
	CAMEROON("Camerún", "Rayo", LightningPower::new),
	NIGERIA("Nigeria", "Tornado", TornadoPower::new ),
	USA("Estados Unidos", "Ilusión", IllusionPower::new);

	private final String displayName;
	private final String powerName;
	private final Supplier<Power> powerFactory;

	Country(String displayName, String powerName, Supplier<Power> powerFactory) {
		this.displayName = displayName;
		this.powerName = powerName;
		this.powerFactory = powerFactory;
	}

	/**
	 * Returns the localized display name of the country.
	 *
	 * @return Country display name
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Returns the display name of the country's special power.
	 *
	 * @return Power name
	 */
	public String getPowerName() {
		return powerName;
	}

	/**
	 * Instantiates a fresh special power instance for this country.
	 *
	 * @return New Power instance
	 */
	public Power getPower(){
		return powerFactory.get();
	}

	/**
	 * Returns the country display name.
	 *
	 * @return Country display name
	 */
	@Override
	public String toString() {
		return displayName;
	}
}
