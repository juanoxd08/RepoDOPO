package domain;

public class DOPOSoccerException extends Exception{
    public static final String INVALID_MATCH_CONFIG = "Configuracion de la partida invalida";
    public static final String INVALID_STAT = "Estadistica invalida";
    public static final String INVALID_STATE = "Estado invalido";
    public static final String NOT_ENOUGH_POINTS = "puntos no suficientes";
    public static final String NULL_ARGUMENT = "argumento nulo";
    public static final String MODALITY_NOT_SUPPORTED = "modalidad no soportada";

    public DOPOSoccerException(String message) {
        super(message);
    }

    
}