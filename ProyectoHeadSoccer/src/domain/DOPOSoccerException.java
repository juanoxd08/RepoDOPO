package domain;

/**
 * Domain-specific checked exception thrown on invalid DOPOSoccer configuration,
 * state transitions, or unsupported operations.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class DOPOSoccerException extends Exception{
    public static final String INVALID_MATCH_CONFIG = "Configuracion de la partida invalida";
    public static final String INVALID_STAT = "Estadistica invalida";
    public static final String INVALID_STATE = "Estado invalido";
    public static final String NOT_ENOUGH_POINTS = "puntos no suficientes";
    public static final String NULL_ARGUMENT = "argumento nulo";
    public static final String MODALITY_NOT_SUPPORTED = "modalidad no soportada";
    public static final String SAVE_ERROR = "No se pudo guardar la partida";
    public static final String OPEN_ERROR = "No se pudo abrir la partida";

    /**
     * Constructs a DOPOSoccerException with the specified detail message.
     *
     * @param message Error description
     */
    public DOPOSoccerException(String message) {
        super(message);
    }

    
}