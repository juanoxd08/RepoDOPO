package domain;

import java.util.function.Supplier;

/**
 * Personajes disponibles. Cada pais tiene un nombre para mostrar y el nombre de su tiro de poder.
 * Los cuatro primeros son los del Anexo 1 del enunciado.
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

	public String getDisplayName() {
		return displayName;
	}

	public String getPowerName() {
		return powerName;
	}

	public Power getPower(){
		return powerFactory.get();
	}

	@Override
	public String toString() {
		return displayName;
	}
}
