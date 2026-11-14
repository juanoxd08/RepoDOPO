package domain;

/**
 * Main facade representing the DOPOSoccer domain game model.
 * Coordinates menu navigation flow, match configuration, and match lifecycle execution.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class HeadSoccer {
    private Match currentMatch;
    private GameStatus state;
    private String message = "";

    // Configuration selections for New Game setup flow
    private boolean vsMachine;
    private MachineProfile machineProfile;
    private Country player1Country;
    private Country player2Country;
    private MatchType matchType;
    private int matchDurationMinutes;
    private int targetGoals;
    private FieldResolution resolution;

    /**
     * Constructs a game session initialized at the main menu.
     */
    public HeadSoccer() {
        this.state = GameStatus.MENU;
        this.vsMachine = false;
        this.machineProfile = MachineProfile.EXPERTA;
        this.player1Country = Country.SOUTH_KOREA;
        this.player2Country = Country.CAMEROON;
        this.matchType = MatchType.TIME;
        this.matchDurationMinutes = 1;
        this.targetGoals = 3;
        this.resolution = FieldResolution.MEDIUM;

        // Partida por defecto lista en caso de inicio rápido
        createDefaultMatch();
    }

    private void createDefaultMatch() {
        Player p1 = new Player("Jugador 1", player1Country, Side.LEFT, createPowerForCountry(player1Country), new PlayerStats(3, 3, 3, 3));
        Player p2 = new Player(vsMachine ? "COM (" + machineProfile.getDisplayName() + ")" : "Jugador 2", 
                               player2Country, Side.RIGHT, createPowerForCountry(player2Country), new PlayerStats(3, 3, 3, 3));

        Controller c1 = new HumanController();
        Controller c2 = vsMachine ? new COMController(Side.RIGHT, machineProfile) : new HumanController();

        int durationSec = matchDurationMinutes * 60;
        this.currentMatch = new Match(p1, p2, c1, c2, matchType, durationSec, targetGoals);
    }

    /**
     * Factory resolving the country's unique special power according to Anexo 1:
     * - SOUTH_KOREA: Aura Azul
     * - CAMEROON: Rayo
     * - NIGERIA: Tornado
     * - USA: Ilusión
     * - CANADA: Congelación
     * 
     * @param country Selected nation
     * @return Special power instance
     */
    public static Power createPowerForCountry(Country country) {
        if (country == null) return new AuraAzulPower();
        switch (country) {
            case SOUTH_KOREA:
                return new AuraAzulPower();
            case CAMEROON:
                return new LightningPower();
            case NIGERIA:
                return new TornadoPower();
            case USA:
                return new IllusionPower();
            case CANADA:
                return new FreezePower();
            case COLOMBIA:
            case BRAZIL:
            case ARGENTINA:
            default:
                return new AuraAzulPower();
        }
    }

    /**
     * Advances match physics and logic if in active gameplay state.
     */
    public void tick() {
        if (state == GameStatus.PLAYING || state == GameStatus.GOAL_SCORED || state == GameStatus.SUDDEN_DEATH) {
            if (currentMatch != null) {
                currentMatch.tick();
                // Sincronizar estado global con el partido
                if (currentMatch.getState() == GameStatus.FINISHED) {
                    this.state = GameStatus.FINISHED;
                }
            }
        }
    }

    /**
     * Starts the new game configuration flow (transitions to SELECT_PLAYERS).
     */
    public void startNewGameFlow() {
        this.state = GameStatus.SELECT_PLAYERS;
    }

    /**
     * Confirms selected players and proceeds to pitch selection (SELECT_CAMP).
     */
    public void confirmPlayersAndProceedToCamp() {
        this.state = GameStatus.SELECT_CAMP;
    }

    /**
     * Instantiates the match using chosen configuration and launches active gameplay.
     */
    public void startActiveMatch() {
        Player p1 = new Player("Jugador 1", player1Country, Side.LEFT, createPowerForCountry(player1Country), new PlayerStats(3, 3, 3, 3));
        String p2Name = vsMachine ? "COM (" + machineProfile.getDisplayName() + ")" : "Jugador 2";
        Player p2 = new Player(p2Name, player2Country, Side.RIGHT, createPowerForCountry(player2Country), new PlayerStats(3, 3, 3, 3));

        Controller c1 = new HumanController();
        Controller c2 = vsMachine ? new COMController(Side.RIGHT, machineProfile) : new HumanController();

        int durationSec = matchDurationMinutes * 60;
        this.currentMatch = new Match(p1, p2, c1, c2, matchType, durationSec, targetGoals);
        this.state = GameStatus.PLAYING;
    }

    /**
     * Toggles between paused and active playing state.
     */
    public void togglePause() {
        if (state == GameStatus.PLAYING) {
            this.state = GameStatus.PAUSED;
            if (currentMatch != null) currentMatch.setState(GameStatus.PAUSED);
        } else if (state == GameStatus.PAUSED) {
            this.state = GameStatus.PLAYING;
            if (currentMatch != null) currentMatch.setState(GameStatus.PLAYING);
        }
    }

    /**
     * Resets player and ball positions, resuming play.
     */
    public void reset() {
        if (currentMatch != null) {
            currentMatch.resetPositions();
            this.state = GameStatus.PLAYING;
            currentMatch.setState(GameStatus.PLAYING);
        }
    }

    /**
     * Returns to main menu screen.
     */
    public void returnToMenu() {
        this.state = GameStatus.MENU;
    }

    public Match getCurrentMatch() { return currentMatch; }
    public void setCurrentMatch(Match currentMatch) { this.currentMatch = currentMatch; }
    public Player getPlayer1() { return currentMatch != null ? currentMatch.getPlayer1() : null; }
    public Player getPlayer2() { return currentMatch != null ? currentMatch.getPlayer2() : null; }
    public Ball getBall() { return currentMatch != null ? currentMatch.getBall() : null; }
    public Field getField() { return currentMatch != null ? currentMatch.getField() : null; }
    public ScoreBoard getScoreBoard() { return currentMatch != null ? currentMatch.getScoreBoard() : null; }
    public Controller getController1() { return currentMatch != null ? currentMatch.getController1() : null; }
    public Controller getController2() { return currentMatch != null ? currentMatch.getController2() : null; }

    public GameStatus getState() {
        return state;
    }

    public void setState(GameStatus state) {
        this.state = state;
        if (currentMatch != null) {
            currentMatch.setState(state);
        }
    }

    /**
     * Returns the match countdown timer.
     * 
     * @return MatchTimer reference from the current match
     */
    public MatchTimer getTimer() {
        return currentMatch != null ? currentMatch.getTimer() : null;
    }

    public String getMessage() {
        if (currentMatch != null && !currentMatch.getWinnerNotification().isEmpty()) {
            return currentMatch.getWinnerNotification();
        }
        return message;
    }

    public void setMessage(String message) { this.message = message; }

    public boolean isVsMachine() { return vsMachine; }
    public void setVsMachine(boolean vsMachine) { this.vsMachine = vsMachine; }
    public MachineProfile getMachineProfile() { return machineProfile; }
    public void setMachineProfile(MachineProfile profile) { this.machineProfile = profile; }
    public Country getPlayer1Country() { return player1Country; }
    public void setPlayer1Country(Country country) { this.player1Country = country; }
    public Country getPlayer2Country() { return player2Country; }
    public void setPlayer2Country(Country country) { this.player2Country = country; }
    public MatchType getMatchType() { return matchType; }
    public void setMatchType(MatchType type) { this.matchType = type; }
    public int getMatchDurationMinutes() { return matchDurationMinutes; }
    public void setMatchDurationMinutes(int minutes) { this.matchDurationMinutes = minutes; }
    public int getTargetGoals() { return targetGoals; }
    public void setTargetGoals(int goals) { this.targetGoals = goals; }
    public FieldResolution getResolution() { return resolution; }
    public void setResolution(FieldResolution res) { this.resolution = res; }
}
