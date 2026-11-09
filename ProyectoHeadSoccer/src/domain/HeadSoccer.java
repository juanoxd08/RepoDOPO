package domain;

public class HeadSoccer {
    private Match currentMatch;
    private String message = "";

    public HeadSoccer() {
        // Configuracion inicial por defecto: Jugador 1 (Humano) vs COM
        Player p1 = new Player("Jugador 1", Country.COLOMBIA, Side.LEFT, new FreezePower(), new PlayerStats());
        Player p2 = new Player("COM", Country.KOREA, Side.RIGHT, new FreezePower(), new PlayerStats());

        Controller c1 = new HumanController();
        Controller c2 = new COMController(Side.RIGHT);

        this.currentMatch = new Match(p1, p2, c1, c2);
    }

    public HeadSoccer(Match match) {
        this.currentMatch = match;
    }

    public void tick() {
        if (currentMatch != null) {
            currentMatch.tick();
        }
    }

    public Match getCurrentMatch() {
        return currentMatch;
    }

    public void setCurrentMatch(Match currentMatch) {
        this.currentMatch = currentMatch;
    }

    public Player getPlayer1() {
        return currentMatch != null ? currentMatch.getPlayer1() : null;
    }

    public Player getPlayer2() {
        return currentMatch != null ? currentMatch.getPlayer2() : null;
    }

    public Ball getBall() {
        return currentMatch != null ? currentMatch.getBall() : null;
    }

    public Field getField() {
        return currentMatch != null ? currentMatch.getField() : null;
    }

    public ScoreBoard getScoreBoard() {
        return currentMatch != null ? currentMatch.getScoreBoard() : null;
    }

    public GameStatus getState() {
        return currentMatch != null ? currentMatch.getState() : GameStatus.PLAYING;
    }

    public void setState(GameStatus state) {
        if (currentMatch != null) {
            currentMatch.setState(state);
        }
    }

    public Controller getController1() {
        return currentMatch != null ? currentMatch.getController1() : null;
    }

    public Controller getController2() {
        return currentMatch != null ? currentMatch.getController2() : null;
    }

    public void togglePause() {
        if (currentMatch != null) {
            if (currentMatch.getState() == GameStatus.PLAYING) {
                currentMatch.setState(GameStatus.PAUSED);
            } else if (currentMatch.getState() == GameStatus.PAUSED) {
                currentMatch.setState(GameStatus.PLAYING);
            }
        }
    }

    public void reset() {
        if (currentMatch != null) {
            currentMatch.resetPositions();
            currentMatch.setState(GameStatus.PLAYING);
        }
    }

    public MatchTimer getTimer() {
        return null; // En partidos sin limite de tiempo no hay timer
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
