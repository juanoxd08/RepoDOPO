package domain;

/**
 * Estados del juego.
 * MENU, SELECT_PLAYERS, SELECT_CAMP y DRAW vienen de la version de Cristian (navegacion de pantallas);
 * segun la hoja de ruta deben salir del dominio en el paso 1.4.
 */
public enum GameStatus {
	NOT_STARTED,
	MENU,
	SELECT_PLAYERS,
	SELECT_CAMP,
	PLAYING,
	PAUSED,
	GOAL_SCORED,
	DRAW,
	SUDDEN_DEATH,
	FINISHED
}
