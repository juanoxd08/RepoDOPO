package domain;

/**
 * Main facade representing the POOBSoccer domain game model.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class HeadSoccer {
    private Match currentMatch;
    private String message = "";

    /**
     * Constructs a game session with default configuration.
     */
    public HeadSoccer() {
        Player p1 = new Player("Jugador 1", Country.COLOMBIA, Side.LEFT, new FreezePower(), new PlayerStats());
        Player p2 = new Player("COM", Country.KOREA, Side.RIGHT, new FreezePower(), new PlayerStats());

        Controller c1 = new HumanController();
        Controller c2 = new COMController(Side.RIGHT);

        this.currentMatch = new Match(p1, p2, c1, c2);
    }

    /**
     * Constructs a game session using a specific match instance.
     * 
     * @param match Match instance to manage
     */
    public HeadSoccer(Match match) {
        this.currentMatch = match;
    }

    /**
     * Advances the match logic by one frame tick.
     */
    public void tick() {
        if (currentMatch != null) {
            currentMatch.tick();
        }
    }

    /**
     * Returns the current match instance.
     * 
     * @return Current match session
     */
    public Match getCurrentMatch() {
        return currentMatch;
    }

    /**
     * Sets the current match instance.
     * 
     * @param currentMatch Match session to assign
     */
    public void setCurrentMatch(Match currentMatch) {
        this.currentMatch = currentMatch;
    }

    /**
     * Returns player 1 from the active match.
     * 
     * @return Player 1 reference, or null if no match exists
     */
    public Player getPlayer1() {
        return currentMatch != null ? currentMatch.getPlayer1() : null;
    }

    /**
     * Returns player 2 from the active match.
     * 
     * @return Player 2 reference, or null if no match exists
     */
    public Player getPlayer2() {
        return currentMatch != null ? currentMatch.getPlayer2() : null;
    }

    /**
     * Returns the match ball.
     * 
     * @return Ball reference, or null if no match exists
     */
    public Ball getBall() {
        return currentMatch != null ? currentMatch.getBall() : null;
    }

    /**
     * Returns the soccer field.
     * 
     * @return Field reference, or null if no match exists
     */
    public Field getField() {
        return currentMatch != null ? currentMatch.getField() : null;
    }

    /**
     * Returns the scoreboard of the match.
     * 
     * @return ScoreBoard reference, or null if no match exists
     */
    public ScoreBoard getScoreBoard() {
        return currentMatch != null ? currentMatch.getScoreBoard() : null;
    }

    /**
     * Returns the current status of the game.
     * 
     * @return Current GameStatus
     */
    public GameStatus getState() {
        return currentMatch != null ? currentMatch.getState() : GameStatus.PLAYING;
    }

    /**
     * Sets the game status.
     * 
     * @param state New GameStatus to apply
     */
    public void setState(GameStatus state) {
        if (currentMatch != null) {
            currentMatch.setState(state);
        }
    }

    /**
     * Returns the controller for player 1.
     * 
     * @return Controller 1 instance
     */
    public Controller getController1() {
        return currentMatch != null ? currentMatch.getController1() : null;
    }

    /**
     * Returns the controller for player 2.
     * 
     * @return Controller 2 instance
     */
    public Controller getController2() {
        return currentMatch != null ? currentMatch.getController2() : null;
    }

    /**
     * Toggles between paused and active playing state.
     */
    public void togglePause() {
        if (currentMatch != null) {
            if (currentMatch.getState() == GameStatus.PLAYING) {
                currentMatch.setState(GameStatus.PAUSED);
            } else if (currentMatch.getState() == GameStatus.PAUSED) {
                currentMatch.setState(GameStatus.PLAYING);
            }
        }
    }

    /**
     * Resets player and ball positions, setting the state back to PLAYING.
     */
    public void reset() {
        if (currentMatch != null) {
            currentMatch.resetPositions();
            currentMatch.setState(GameStatus.PLAYING);
        }
    }

    /**
     * Returns the match timer.
     * 
     * @return MatchTimer reference or null
     */
    public MatchTimer getTimer() {
        return null;
    }

    /**
     * Returns the current status notification message.
     * 
     * @return Descriptive message text
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the current status notification message.
     * 
     * @param message Text to display
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
