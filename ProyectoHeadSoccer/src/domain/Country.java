package domain;

/**
 * Personajes disponibles. Cada pais tiene un nombre para mostrar y el nombre de su tiro de poder.
 * Los cuatro primeros son los del Anexo 1 del enunciado.
 */
public enum Country {
	SOUTH_KOREA("Corea del Sur", "Aura Azul"),
	CAMEROON("Camerún", "Rayo"),
	NIGERIA("Nigeria", "Tornado"),
	USA("Estados Unidos", "Ilusión"),
	COLOMBIA("Colombia", "Tiro Fuego"),
	CANADA("Canadá", "Congelación"),
	BRAZIL("Brasil", "Samba"),
	ARGENTINA("Argentina", "Tango");

	private final String displayName;
	private final String powerName;

	Country(String displayName, String powerName) {
		this.displayName = displayName;
		this.powerName = powerName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getPowerName() {
		return powerName;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
