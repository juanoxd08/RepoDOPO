package presentation;

import domain.*;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * Graphical canvas rendering all screens and match entities using only
 * DOPOSoccer, MatchView, PlayerView, BallView, and MatchListener.
 *
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class PhotoHeadSoccer extends JPanel implements MatchListener {
    private DOPOSoccer game;
    private HeadSoccerGUI parentFrame;

    private Modality modality = Modality.PVP;
    private Country leftCountry = Country.SOUTH_KOREA;
    private Country rightCountry = Country.CAMEROON;
    private MatchType matchType = MatchType.TIME;
    private int matchDurationMinutes = 2;
    private int targetGoals = 3;
    private FieldResolution resolution = FieldResolution.MEDIUM;
    private MachineProfile machineProfile = MachineProfile.EXPERTA;

    private String lastScorerText = "¡Anotación!";
    private String winnerNotification = "Fin del juego";

    public final java.awt.Rectangle btnNewGame = new java.awt.Rectangle(0, 0, 0, 0);
    public final java.awt.Rectangle btnLoadGame = new java.awt.Rectangle(0, 0, 0, 0);
    public final java.awt.Rectangle btnSaveGame = new java.awt.Rectangle(0, 0, 0, 0);
    public final java.awt.Rectangle btnContinue = new java.awt.Rectangle(0, 0, 0, 0);
    public final java.awt.Rectangle btnStart = new java.awt.Rectangle(0, 0, 0, 0);
    public final java.awt.Rectangle btnMenu = new java.awt.Rectangle(0, 0, 0, 0);

    public final java.awt.Rectangle btnModePvP = new java.awt.Rectangle(0, 0, 0, 0);
    public final java.awt.Rectangle btnModePvM = new java.awt.Rectangle(0, 0, 0, 0);
    public final java.awt.Rectangle btnTypeTime = new java.awt.Rectangle(0, 0, 0, 0);
    public final java.awt.Rectangle btnTypeGoals = new java.awt.Rectangle(0, 0, 0, 0);
    public final java.awt.Rectangle btnNextP1Country = new java.awt.Rectangle(0, 0, 0, 0);
    public final java.awt.Rectangle btnNextP2Country = new java.awt.Rectangle(0, 0, 0, 0);
    public final java.awt.Rectangle btnNextProfile = new java.awt.Rectangle(0, 0, 0, 0);
    public final java.awt.Rectangle btnResSmall = new java.awt.Rectangle(0, 0, 0, 0);
    public final java.awt.Rectangle btnResMed = new java.awt.Rectangle(0, 0, 0, 0);
    public final java.awt.Rectangle btnResLarge = new java.awt.Rectangle(0, 0, 0, 0);

    /**
     * Constructs the main graphical panel and registers it as a match listener.
     *
     * @param game Domain facade instance
     * @param parentFrame Parent application window managing navigation screens
     */
    public PhotoHeadSoccer(DOPOSoccer game, HeadSoccerGUI parentFrame) {
        this.game = game;
        this.parentFrame = parentFrame;
        this.game.addListener(this);
        int screenW = resolution.getPixelWidth();
        int screenH = resolution.getPixelHeight();
        this.setPreferredSize(new Dimension(screenW, screenH));
        this.setBackground(new Color(135, 206, 235));
    }

    /**
     * Builds a MatchConfig from the current UI selections and starts a new match.
     */
    public void startConfiguredMatch() {
        int target = (matchType == MatchType.TIME) ? matchDurationMinutes : targetGoals;
        try {
            MatchConfig config = MatchConfig.builder()
                    .modality(modality)
                    .leftCountry(leftCountry)
                    .rightCountry(rightCountry)
                    .matchType(matchType)
                    .targetValue(target)
                    .build();
            this.lastScorerText = "¡Anotación!";
            this.winnerNotification = "Fin del juego";
            game.newMatch(config);
            parentFrame.setCurrentScreen(Screen.MATCH);
        } catch (DOPOSoccerException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de configuración", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Updates the goal banner text with the scoring player's name.
     *
     * @param scorer Side of the player who scored
     */
    @Override
    public void onGoal(Side scorer) {
        MatchView match = game.getMatch();
        if (match != null && match.getPlayer(scorer) != null) {
            lastScorerText = "¡GOL DE " + match.getPlayer(scorer).getName().toUpperCase() + "!";
        } else {
            lastScorerText = "¡GOL!";
        }
    }

    /**
     * Updates the status banner when a player is stunned due to zero health.
     *
     * @param victim Side of the stunned player
     */
    @Override
    public void onStunned(Side victim) {
        lastScorerText = "¡JUGADOR ATURDIDO!";
    }

    /**
     * Updates the end-of-match notification message with the winner and awarded points.
     *
     * @param winner Winning side, or null if tied
     * @param winnerPoints Points awarded to the winner
     * @param loserPoints Points awarded to the loser
     */
    @Override
    public void onMatchFinished(Side winner, int winnerPoints, int loserPoints) {
        if (winner == null) {
            winnerNotification = "¡EMPATE TÉCNICO! (+" + winnerPoints + " PUNTOS)";
            return;
        }
        MatchView match = game.getMatch();
        String name = (match != null && match.getPlayer(winner) != null)
                ? match.getPlayer(winner).getName().toUpperCase()
                : winner.name();
        winnerNotification = "¡GANADOR: " + name + "! (+" + winnerPoints + " PUNTOS)";
    }

    /**
     * Renders the active application screen onto the panel.
     *
     * @param g Swing graphics context
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Screen screen = parentFrame.getCurrentScreen();
        if (screen == Screen.MENU) {
            drawMenuScreen(g2d);
        } else if (screen == Screen.SELECT_PLAYERS) {
            drawSelectPlayersScreen(g2d);
        } else if (screen == Screen.SELECT_CAMP) {
            drawSelectCampScreen(g2d);
        } else {
            MatchView match = game.getMatch();
            if (match != null) {
                drawMatchScreen(g2d, match);
            }
        }
    }

    /*
     * Renders the main menu screen with New Game, Load Game, and keyboard controls.
     */
    private void drawMenuScreen(Graphics2D g2d) {
        int w = getWidth();
        int h = getHeight();

        drawBackgroundField(g2d);

        g2d.setColor(new Color(0, 0, 0, 140));
        g2d.fillRect(0, 0, w, h);

        g2d.setColor(new Color(255, 60, 60));
        g2d.fillRoundRect(w / 2 - 260, h / 6 - 45, 520, 80, 40, 40);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRoundRect(w / 2 - 260, h / 6 - 45, 520, 80, 40, 40);

        g2d.setFont(new Font("Impact", Font.BOLD, 46));
        g2d.setColor(Color.YELLOW);
        String title = "HEAD SOCCER GAME";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(title, w / 2 - fm.stringWidth(title) / 2, h / 6 + 12);

        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.setColor(Color.WHITE);
        String sub = "DOPOSoccer 2026-2  •  Proyecto Final POOB";
        g2d.drawString(sub, w / 2 - g2d.getFontMetrics().stringWidth(sub) / 2, h / 6 + 65);

        btnNewGame.setBounds(w / 2 - 270, h / 2 - 25, 250, 56);
        drawGlossyButton(g2d, btnNewGame, "NEW GAME", new Color(245, 175, 25), Color.BLACK, 22);

        btnLoadGame.setBounds(w / 2 + 20, h / 2 - 25, 250, 56);
        drawGlossyButton(g2d, btnLoadGame, "ABRIR PARTIDA", new Color(70, 150, 220), Color.WHITE, 20);

        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRoundRect(w / 2 - 280, h / 2 + 65, 560, 120, 18, 18);
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 15));
        g2d.drawString("CONTROLES DE JUEGO (Mismo Teclado):", w / 2 - 250, h / 2 + 95);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.drawString("• Jugador 1: A / D (Mover), W (Saltar), S (Patear), Q (Activar Poder)", w / 2 - 250, h / 2 + 125);
        g2d.drawString("• Jugador 2: ← / → (Mover), ↑ (Saltar), ↓ (Patear), Ctrl Der (Activar Poder)", w / 2 - 250, h / 2 + 155);

        g2d.setFont(new Font("Arial", Font.ITALIC, 13));
        g2d.setColor(new Color(200, 255, 200));
        String tip = "Haz clic en 'NEW GAME' o presiona ENTER para configurar tu partido";
        g2d.drawString(tip, w / 2 - g2d.getFontMetrics().stringWidth(tip) / 2, h - 30);
    }


    /*
     * Renders the player, modality, machine profile, and match type selection screen.
     */
    private void drawSelectPlayersScreen(Graphics2D g2d) {
        int w = getWidth();
        int h = getHeight();

        drawBackgroundField(g2d);

        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRect(0, 0, w, h);

        g2d.setFont(new Font("Impact", Font.BOLD, 42));
        g2d.setColor(Color.YELLOW);
        String title = "SELECT PLAYERS";
        g2d.drawString(title, w / 2 - g2d.getFontMetrics().stringWidth(title) / 2, 55);

        int panelW = Math.min(760, w - 40);
        int panelH = h - 160;
        int px = w / 2 - panelW / 2;
        int py = 75;

        g2d.setColor(new Color(25, 30, 40, 230));
        g2d.fillRoundRect(px, py, panelW, panelH, 20, 20);
        g2d.setColor(new Color(255, 200, 50));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(px, py, panelW, panelH, 20, 20);

        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Modalidad:", px + 30, py + 40);

        boolean vsMachine = (modality == Modality.PVC);
        btnModePvP.setBounds(px + 140, py + 18, 160, 34);
        drawToggleOption(g2d, btnModePvP, "Jugador vs Jugador", !vsMachine);

        btnModePvM.setBounds(px + 320, py + 18, 160, 34);
        drawToggleOption(g2d, btnModePvM, "Jugador vs Máquina", vsMachine);

        if (vsMachine) {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawString("Perfil IA:", px + 500, py + 40);
            btnNextProfile.setBounds(px + 580, py + 18, 140, 34);
            drawToggleOption(g2d, btnNextProfile, machineProfile.getDisplayName() + " ↻", true);
        }

        int colW = panelW / 2 - 40;

        int p1X = px + 30;
        int p1Y = py + 75;
        g2d.setColor(new Color(40, 45, 60));
        g2d.fillRoundRect(p1X, p1Y, colW, 160, 15, 15);
        g2d.setColor(new Color(255, 100, 100));
        g2d.drawRoundRect(p1X, p1Y, colW, 160, 15, 15);

        g2d.setFont(new Font("Arial", Font.BOLD, 17));
        g2d.setColor(Color.WHITE);
        g2d.drawString("JUGADOR 1 (Izquierda)", p1X + 15, p1Y + 30);

        g2d.setFont(new Font("Arial", Font.PLAIN, 15));
        g2d.drawString("País: " + leftCountry.getDisplayName(), p1X + 15, p1Y + 65);
        g2d.drawString("Poder: " + leftCountry.getPowerName(), p1X + 15, p1Y + 95);

        btnNextP1Country.setBounds(p1X + 15, p1Y + 115, colW - 30, 32);
        drawGlossyButton(g2d, btnNextP1Country, "Cambiar Personaje ↻", new Color(70, 130, 180), Color.WHITE, 14);

        int p2X = px + panelW / 2 + 10;
        int p2Y = py + 75;
        g2d.setColor(new Color(40, 45, 60));
        g2d.fillRoundRect(p2X, p2Y, colW, 160, 15, 15);
        g2d.setColor(new Color(100, 150, 255));
        g2d.drawRoundRect(p2X, p2Y, colW, 160, 15, 15);

        g2d.setFont(new Font("Arial", Font.BOLD, 17));
        g2d.setColor(Color.WHITE);
        g2d.drawString(vsMachine ? "MÁQUINA (Derecha)" : "JUGADOR 2 (Derecha)", p2X + 15, p2Y + 30);

        g2d.setFont(new Font("Arial", Font.PLAIN, 15));
        g2d.drawString("País: " + rightCountry.getDisplayName(), p2X + 15, p2Y + 65);
        g2d.drawString("Poder: " + rightCountry.getPowerName(), p2X + 15, p2Y + 95);

        btnNextP2Country.setBounds(p2X + 15, p2Y + 115, colW - 30, 32);
        drawGlossyButton(g2d, btnNextP2Country, "Cambiar Personaje ↻", new Color(70, 130, 180), Color.WHITE, 14);

        int ty = py + 255;
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Tipo de Partido:", px + 30, ty + 25);

        btnTypeTime.setBounds(px + 170, ty + 5, 200, 34);
        drawToggleOption(g2d, btnTypeTime, "Por Tiempo (" + matchDurationMinutes + " min) ↻", matchType == MatchType.TIME);

        btnTypeGoals.setBounds(px + 390, ty + 5, 200, 34);
        drawToggleOption(g2d, btnTypeGoals, "Por Goles (" + targetGoals + " goles) ↻", matchType == MatchType.GOALS);

        btnContinue.setBounds(w / 2 - 120, h - 70, 240, 48);
        drawGlossyButton(g2d, btnContinue, "CONTINUE ➔", new Color(245, 175, 25), Color.BLACK, 20);
    }


    /*
     * Renders the field resolution selection screen before match start.
     */
    private void drawSelectCampScreen(Graphics2D g2d) {
        int w = getWidth();
        int h = getHeight();

        drawBackgroundField(g2d);

        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRect(0, 0, w, h);

        g2d.setFont(new Font("Impact", Font.BOLD, 42));
        g2d.setColor(Color.YELLOW);
        String title = "SELECT THE CAMP";
        g2d.drawString(title, w / 2 - g2d.getFontMetrics().stringWidth(title) / 2, 60);

        int panelW = 600;
        int panelH = 260;
        int px = w / 2 - panelW / 2;
        int py = 90;

        g2d.setColor(new Color(25, 30, 40, 230));
        g2d.fillRoundRect(px, py, panelW, panelH, 20, 20);
        g2d.setColor(new Color(255, 200, 50));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(px, py, panelW, panelH, 20, 20);

        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Selecciona la Resolución de Cancha:", px + 40, py + 40);

        btnResSmall.setBounds(px + 50, py + 70, 500, 42);
        drawToggleOption(g2d, btnResSmall, FieldResolution.SMALL.getLabel(), resolution == FieldResolution.SMALL);

        btnResMed.setBounds(px + 50, py + 125, 500, 42);
        drawToggleOption(g2d, btnResMed, FieldResolution.MEDIUM.getLabel(), resolution == FieldResolution.MEDIUM);

        btnResLarge.setBounds(px + 50, py + 180, 500, 42);
        drawToggleOption(g2d, btnResLarge, FieldResolution.LARGE.getLabel(), resolution == FieldResolution.LARGE);

        btnStart.setBounds(w / 2 - 130, h - 85, 260, 54);
        drawGlossyButton(g2d, btnStart, "⚽ START ⚽", new Color(40, 180, 40), Color.WHITE, 24);
    }


    /*
     * Renders the active match screen, scaled pitch entities, HUD, and state overlays.
     */
    private void drawMatchScreen(Graphics2D g2d, MatchView match) {
        if (match == null) return;
        int fieldW = match.getFieldWidth();
        int fieldH = match.getFieldHeight();
        double scaleX = (double) getWidth() / fieldW;
        double scaleY = (double) getHeight() / fieldH;
        double currentScale = Math.min(scaleX, scaleY);

        Graphics2D gField = (Graphics2D) g2d.create();
        gField.scale(currentScale, currentScale);

        drawField(gField, match);

        PlayerView left = match.getPlayer(Side.LEFT);
        PlayerView right = match.getPlayer(Side.RIGHT);
        if (left != null) drawPlayer(gField, left);
        if (right != null) drawPlayer(gField, right);
        drawBall(gField, match);

        gField.dispose();

        drawHUD(g2d, match);

        GameStatus status = match.getStatus();
        if (status == GameStatus.PAUSED) {
            drawPauseOverlay(g2d);
        } else if (status == GameStatus.GOAL_SCORED) {
            drawGoalOverlay(g2d);
        } else if (status == GameStatus.SUDDEN_DEATH) {
            drawDrawOverlay(g2d);
        } else if (status == GameStatus.FINISHED) {
            drawFinishedOverlay(g2d);
        }
    }

    /*
     * Renders the decorative stadium sky, stands, and turf background for menu screens.
     */
    private void drawBackgroundField(Graphics2D g2d) {
        int w = getWidth();
        int h = getHeight();
        g2d.setColor(new Color(135, 206, 235));
        g2d.fillRect(0, 0, w, h);
        g2d.setColor(new Color(90, 110, 130));
        g2d.fillRect(0, h / 3, w, h / 3);
        g2d.setColor(new Color(46, 139, 87));
        g2d.fillRect(0, (int) (h * 0.65), w, (int) (h * 0.35));
    }

    /**
     * Renders the soccer pitch, stadium banner, field lines, and both goals.
     *
     * @param g Graphics context scaled to field coordinates
     * @param match Read-only match view
     */
    public void drawField(Graphics g, MatchView match) {
        Graphics2D g2d = (Graphics2D) g;
        int fieldW = match.getFieldWidth();
        int fieldH = match.getFieldHeight();

        g2d.setColor(new Color(95, 115, 135));
        g2d.fillRect(0, 0, fieldW, 68);

        g2d.setColor(new Color(35, 35, 35));
        g2d.fillRect(0, 68, fieldW, 14);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 7));
        g2d.drawString("DOPOSoccer 2026-2  •  HEAD SOCCER", 15, 78);
        g2d.drawString("ESCUELA COLOMBIANA DE INGENIERÍA", 190, 78);

        g2d.setColor(new Color(46, 139, 87));
        g2d.fillRect(0, 82, fieldW, fieldH - 82);

        g2d.setColor(new Color(56, 155, 98));
        for (int i = 0; i < fieldW; i += 30) {
            g2d.fillRect(i, 82, 15, fieldH - 82);
        }

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawLine(0, fieldH - 1, fieldW, fieldH - 1);
        g2d.drawLine(fieldW / 2, 82, fieldW / 2, fieldH);
        g2d.drawOval(fieldW / 2 - 25, 120, 50, 40);

        drawGoal(g2d, match, Side.LEFT);
        drawGoal(g2d, match, Side.RIGHT);
    }

    /*
     * Renders the goal net and crossbar for the specified side.
     */
    private void drawGoal(Graphics2D g2d, MatchView match, Side side) {
        domain.Rectangle area = match.getGoalArea(side);
        if (area != null) {
            int rx = (int) area.getX();
            int ry = (int) area.getY();
            int rw = (int) area.getWidth();
            int rh = (int) area.getHeight();

            g2d.setColor(new Color(240, 240, 240, 160));
            g2d.fillRect(rx, ry, rw, rh);
            g2d.setColor(new Color(160, 160, 160, 200));
            g2d.setStroke(new BasicStroke(0.6f));
            for (int x = rx; x <= rx + rw; x += 5) g2d.drawLine(x, ry, x, ry + rh);
            for (int y = ry; y <= ry + rh; y += 5) g2d.drawLine(rx, y, rx + rw, y);
        }

        domain.Rectangle bar = match.getCrossBar(side);
        if (bar != null) {
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2f));
            g2d.drawRect((int) bar.getX(), (int) bar.getY(), (int) bar.getWidth(), (int) bar.getHeight());
        }
    }

    /**
     * Renders a player's limbs, active power aura, and status effect indicator.
     *
     * @param g Graphics context scaled to field coordinates
     * @param p Read-only player view
     */
    public void drawPlayer(Graphics g, PlayerView p) {
        if (p == null || p.getHeadShape() == null) return;
        Graphics2D g2d = (Graphics2D) g;

        Circle hc = p.getHeadShape();
        int hx = (int) hc.getX();
        int hy = (int) hc.getY();
        int hr = (int) hc.getRadius();

        Color baseColor = getColorByCountry(p.getCountry());

        if (p.hasAura()) {
            g2d.setColor(new Color(255, 220, 50, 90));
            g2d.fillOval(hx - hr - 10, hy - hr - 10, (hr + 10) * 2, (hr + 10) * 2);
            g2d.setColor(new Color(255, 160, 0, 140));
            g2d.setStroke(new BasicStroke(2f));
            g2d.drawOval(hx - hr - 6, hy - hr - 6, (hr + 6) * 2, (hr + 6) * 2);
        }

        String effectName = p.getActiveEffectName();
        if (p.isStunned() || (effectName != null && !effectName.isEmpty())) {
            g2d.setColor(new Color(255, 255, 255, 180));
            g2d.setFont(new Font("Arial", Font.BOLD, 7));
            String tag = (effectName != null && !effectName.isEmpty()) ? effectName : "STUN";
            g2d.drawString("★ " + tag + " ★", hx - 14, hy - hr - 8);
        }

        domain.Rectangle br = p.getBodyShape();
        if (br != null) {
            g2d.setColor(baseColor.darker());
            g2d.fillRoundRect((int) br.getX(), (int) br.getY(), (int) br.getWidth(), (int) br.getHeight(), 5, 5);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1f));
            g2d.drawRoundRect((int) br.getX(), (int) br.getY(), (int) br.getWidth(), (int) br.getHeight(), 5, 5);
        }

        g2d.setColor(baseColor);
        g2d.fillOval(hx - hr, hy - hr, hr * 2, hr * 2);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawOval(hx - hr, hy - hr, hr * 2, hr * 2);

        g2d.setColor(Color.WHITE);
        int eyeX = (p.getSide() == Side.LEFT) ? hx + hr / 3 : hx - hr / 3;
        g2d.fillOval(eyeX - 3, hy - 3, 6, 7);
        g2d.setColor(Color.BLACK);
        g2d.fillOval(eyeX - (p.getSide() == Side.LEFT ? 1 : 2), hy - 2, 3, 4);

        Circle fc = p.getFootShape();
        if (fc != null) {
            int fx = (int) fc.getX();
            int fy = (int) fc.getY();
            int fr = (int) fc.getRadius();
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillOval(fx - fr, fy - fr, fr * 2, fr * 2);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(fx - fr, fy - fr, fr * 2, fr * 2);
        }
    }

    /*
     * Resolves the display color associated with a playable country.
     */
    private Color getColorByCountry(Country c) {
        if (c == null) return Color.RED;
        switch (c) {
            case SOUTH_KOREA: return new Color(230, 60, 60);
            case CAMEROON: return new Color(30, 160, 80);
            case NIGERIA: return new Color(20, 130, 60);
            case USA: return new Color(60, 100, 220);
            default: return Color.ORANGE;
        }
    }

    /**
     * Renders the soccer ball and its power-shot glow when active.
     *
     * @param g Graphics context scaled to field coordinates
     * @param match Read-only match view
     */
    public void drawBall(Graphics g, MatchView match) {
        BallView ball = match.getBall();
        if (ball == null) return;
        Graphics2D g2d = (Graphics2D) g;

        int bx = (int) ball.getX();
        int by = (int) ball.getY();
        int r = (int) ball.getRadius();

        boolean isPower = ball.hasPowerShot();

        if (isPower) {
            g2d.setColor(new Color(255, 200, 0, 120));
            g2d.fillOval(bx - r - 6, by - r - 6, (r + 6) * 2, (r + 6) * 2);
        }

        g2d.setColor(isPower ? new Color(255, 240, 200) : Color.WHITE);
        g2d.fillOval(bx - r, by - r, r * 2, r * 2);
        g2d.setColor(isPower ? new Color(255, 120, 0) : Color.BLACK);
        g2d.setStroke(new BasicStroke(isPower ? 1.8f : 1.2f));
        g2d.drawOval(bx - r, by - r, r * 2, r * 2);
        g2d.drawLine(bx - r + 2, by, bx + r - 2, by);
        g2d.drawLine(bx, by - r + 2, bx, by + r - 2);
    }

    /**
     * Renders the top HUD including scoreboard, timer or goal target, and player bars.
     *
     * @param g2d Screen-space graphics context
     * @param match Read-only match view
     */
    public void drawHUD(Graphics2D g2d, MatchView match) {
        int w = getWidth();

        g2d.setColor(new Color(15, 20, 25, 230));
        g2d.fillRoundRect(w / 2 - 160, 10, 320, 65, 14, 14);
        g2d.setColor(new Color(245, 195, 35));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(w / 2 - 160, 10, 320, 65, 14, 14);

        int leftGoals = match.getGoals(Side.LEFT);
        int rightGoals = match.getGoals(Side.RIGHT);
        g2d.setFont(new Font("Impact", Font.PLAIN, 36));
        g2d.setColor(Color.WHITE);
        String scoreText = leftGoals + "  -  " + rightGoals;
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(scoreText, w / 2 - fm.stringWidth(scoreText) / 2, 48);

        g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2d.setColor(Color.GREEN);
        String centerInfo;
        if (match.isSuddenDeath()) {
            centerInfo = "GOL DE ORO";
            g2d.setColor(Color.YELLOW);
        } else if (match.getRemainingSeconds() < 0) {
            centerInfo = "META: " + targetGoals + " GOLES";
        } else {
            int sec = match.getRemainingSeconds();
            centerInfo = String.format("%02d:%02d", sec / 60, sec % 60);
        }
        g2d.drawString(centerInfo, w / 2 - g2d.getFontMetrics().stringWidth(centerInfo) / 2, 68);

        PlayerView p1 = match.getPlayer(Side.LEFT);
        if (p1 != null) {
            drawPlayerHUD(g2d, p1, 25, 14, 220);
        }

        PlayerView p2 = match.getPlayer(Side.RIGHT);
        if (p2 != null) {
            drawPlayerHUD(g2d, p2, w - 245, 14, 220);
        }
    }

    /*
     * Renders a single player's name, health bar, and power gauge in the HUD.
     */
    private void drawPlayerHUD(Graphics2D g2d, PlayerView p, int x, int y, int barW) {
        g2d.setFont(new Font("Arial", Font.BOLD, 13));
        g2d.setColor(Color.WHITE);
        String name = p.getName() + " (" + p.getCountry().getDisplayName() + ")";
        g2d.drawString(name, x, y + 12);

        int hpY = y + 18;
        g2d.setColor(Color.BLACK);
        g2d.fillRect(x, hpY, barW, 14);

        double hpRatio = Math.max(0.0, Math.min(1.0, p.getLife() / p.getMaxLife()));
        Color hpColor = (hpRatio > 0.5) ? new Color(40, 200, 40) : (hpRatio > 0.25 ? Color.ORANGE : Color.RED);
        g2d.setColor(hpColor);
        g2d.fillRect(x + 1, hpY + 1, (int) ((barW - 2) * hpRatio), 12);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(x, hpY, barW, 14);

        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        String hpText = (int) p.getLife() + "/" + (int) p.getMaxLife() + " HP";
        g2d.drawString(hpText, x + barW / 2 - g2d.getFontMetrics().stringWidth(hpText) / 2, hpY + 11);

        int powY = hpY + 16;
        g2d.setColor(Color.BLACK);
        g2d.fillRect(x, powY, barW, 10);

        double powRatio = Math.max(0.0, Math.min(1.0, p.getPowerProgress() / 100.0));
        g2d.setColor(powRatio >= 1.0 ? Color.YELLOW : new Color(240, 140, 0));
        g2d.fillRect(x + 1, powY + 1, (int) ((barW - 2) * powRatio), 8);
        g2d.setColor(Color.GRAY);
        g2d.drawRect(x, powY, barW, 10);

        if (p.hasAura()) {
            g2d.setFont(new Font("Arial", Font.BOLD, 9));
            g2d.setColor(Color.YELLOW);
            g2d.drawString("⚡ AURA ACTIVA ⚡", x + barW / 2 - 40, powY + 9);
        }
    }


    /*
     * Renders the pause overlay with resume, menu, and save match controls.
     */
    private void drawPauseOverlay(Graphics2D g2d) {
        int w = getWidth();
        int h = getHeight();
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRect(0, 0, w, h);

        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Impact", Font.BOLD, 54));
        String text = "PAUSE";
        g2d.drawString(text, w / 2 - g2d.getFontMetrics().stringWidth(text) / 2, h / 2 - 30);

        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.setColor(Color.WHITE);
        String s1 = "Presiona 'P' para continuar jugando";
        String s2 = "Presiona 'M' para salir al Menú Principal";
        g2d.drawString(s1, w / 2 - g2d.getFontMetrics().stringWidth(s1) / 2, h / 2 + 15);
        g2d.drawString(s2, w / 2 - g2d.getFontMetrics().stringWidth(s2) / 2, h / 2 + 45);

        btnSaveGame.setBounds(w / 2 - 130, h / 2 + 75, 260, 48);
        drawGlossyButton(g2d, btnSaveGame, "GUARDAR PARTIDA", new Color(40, 170, 80), Color.WHITE, 18);
    }

    /*
     * Renders the goal celebration banner overlay.
     */
    private void drawGoalOverlay(Graphics2D g2d) {
        int w = getWidth();
        int h = getHeight();
        g2d.setColor(new Color(20, 140, 20, 180));
        g2d.fillRect(0, h / 2 - 90, w, 180);

        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Impact", Font.BOLD, 68));
        String goal = "¡¡¡ GOOOOOL !!!";
        g2d.drawString(goal, w / 2 - g2d.getFontMetrics().stringWidth(goal) / 2, h / 2 - 10);

        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        g2d.setColor(Color.WHITE);
        String desc = (lastScorerText != null && !lastScorerText.isEmpty()) ? lastScorerText : "¡Anotación!";
        g2d.drawString(desc, w / 2 - g2d.getFontMetrics().stringWidth(desc) / 2, h / 2 + 40);
    }

    /*
     * Renders the sudden-death golden goal banner overlay.
     */
    private void drawDrawOverlay(Graphics2D g2d) {
        int w = getWidth();
        int h = getHeight();
        g2d.setColor(new Color(200, 120, 20, 190));
        g2d.fillRect(0, h / 2 - 80, w, 160);

        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Impact", Font.BOLD, 54));
        String title = "¡EMPATE! - GOL DE ORO";
        g2d.drawString(title, w / 2 - g2d.getFontMetrics().stringWidth(title) / 2, h / 2);

        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.setColor(Color.WHITE);
        String sub = "Muerte súbita: El primer jugador que marque gana el partido";
        g2d.drawString(sub, w / 2 - g2d.getFontMetrics().stringWidth(sub) / 2, h / 2 + 45);
    }

    /*
     * Renders the match-finished overlay with winner announcement and return-to-menu button.
     */
    private void drawFinishedOverlay(Graphics2D g2d) {
        int w = getWidth();
        int h = getHeight();
        g2d.setColor(new Color(0, 0, 0, 210));
        g2d.fillRect(0, 0, w, h);

        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Impact", Font.BOLD, 52));
        String title = "PARTIDO FINALIZADO";
        g2d.drawString(title, w / 2 - g2d.getFontMetrics().stringWidth(title) / 2, h / 2 - 60);

        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.setColor(new Color(100, 255, 100));
        String winner = (winnerNotification != null && !winnerNotification.isEmpty()) ? winnerNotification : "Fin del juego";
        g2d.drawString(winner, w / 2 - g2d.getFontMetrics().stringWidth(winner) / 2, h / 2 - 10);

        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.setColor(Color.WHITE);
        String pts = "Recompensa: Ganador +100 puntos | Perdedor +25 puntos";
        g2d.drawString(pts, w / 2 - g2d.getFontMetrics().stringWidth(pts) / 2, h / 2 + 30);

        btnMenu.setBounds(w / 2 - 120, h / 2 + 65, 240, 48);
        drawGlossyButton(g2d, btnMenu, "VOLVER AL MENÚ", new Color(245, 175, 25), Color.BLACK, 18);
    }

    /*
     * Renders a rounded action button with centered label text.
     */
    private void drawGlossyButton(Graphics2D g2d, java.awt.Rectangle r, String text, Color bg, Color fg, int fontSize) {
        g2d.setColor(bg);
        g2d.fillRoundRect(r.x, r.y, r.width, r.height, 16, 16);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(r.x, r.y, r.width, r.height, 16, 16);

        g2d.setFont(new Font("Arial", Font.BOLD, fontSize));
        g2d.setColor(fg);
        FontMetrics fm = g2d.getFontMetrics();
        int tx = r.x + (r.width - fm.stringWidth(text)) / 2;
        int ty = r.y + ((r.height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(text, tx, ty);
    }

    /*
     * Renders a selectable toggle button reflecting active or inactive state.
     */
    private void drawToggleOption(Graphics2D g2d, java.awt.Rectangle r, String text, boolean selected) {
        Color bg = selected ? new Color(245, 175, 25) : new Color(50, 55, 65);
        Color fg = selected ? Color.BLACK : Color.WHITE;
        g2d.setColor(bg);
        g2d.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
        g2d.setColor(selected ? Color.WHITE : Color.GRAY);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(r.x, r.y, r.width, r.height, 12, 12);

        g2d.setFont(new Font("Arial", Font.BOLD, 13));
        g2d.setColor(fg);
        FontMetrics fm = g2d.getFontMetrics();
        int tx = r.x + (r.width - fm.stringWidth(text)) / 2;
        int ty = r.y + ((r.height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(text, tx, ty);
    }

    /**
     * Dispatches mouse click events across active UI buttons for the current screen.
     *
     * @param e Mouse click event
     */
    public void handleMouseClick(MouseEvent e) {
        Point p = e.getPoint();
        Screen screen = parentFrame.getCurrentScreen();

        if (screen == Screen.MENU) {
            if (btnNewGame.contains(p)) {
                parentFrame.setCurrentScreen(Screen.SELECT_PLAYERS);
                repaint();
            } else if (btnLoadGame.contains(p)) {
                openSavedMatch();
                repaint();
            }
        } else if (screen == Screen.SELECT_PLAYERS) {
            if (btnModePvP.contains(p)) {
                modality = Modality.PVP;
                repaint();
            } else if (btnModePvM.contains(p)) {
                modality = Modality.PVC;
                repaint();
            } else if (btnNextProfile.contains(p) && modality == Modality.PVC) {
                MachineProfile[] profiles = MachineProfile.values();
                int nextIdx = (machineProfile.ordinal() + 1) % profiles.length;
                machineProfile = profiles[nextIdx];
                repaint();
            } else if (btnNextP1Country.contains(p)) {
                Country[] countries = Country.values();
                int idx = 0;
                for (int i = 0; i < countries.length; i++) {
                    if (countries[i] == leftCountry) idx = (i + 1) % countries.length;
                }
                leftCountry = countries[idx];
                repaint();
            } else if (btnNextP2Country.contains(p)) {
                Country[] countries = {Country.CAMEROON, Country.NIGERIA, Country.USA, Country.SOUTH_KOREA};
                int idx = 0;
                for (int i = 0; i < countries.length; i++) {
                    if (countries[i] == rightCountry) idx = (i + 1) % countries.length;
                }
                rightCountry = countries[idx];
                repaint();
            } else if (btnTypeTime.contains(p)) {
                matchType = MatchType.TIME;
                matchDurationMinutes = (matchDurationMinutes % 3) + 1;
                repaint();
            } else if (btnTypeGoals.contains(p)) {
                matchType = MatchType.GOALS;
                targetGoals = (targetGoals == 3) ? 5 : (targetGoals == 5 ? 7 : 3);
                repaint();
            } else if (btnContinue.contains(p)) {
                parentFrame.setCurrentScreen(Screen.SELECT_CAMP);
                repaint();
            }
        } else if (screen == Screen.SELECT_CAMP) {
            if (btnResSmall.contains(p)) {
                resolution = FieldResolution.SMALL;
                parentFrame.updateResolution(FieldResolution.SMALL);
                repaint();
            } else if (btnResMed.contains(p)) {
                resolution = FieldResolution.MEDIUM;
                parentFrame.updateResolution(FieldResolution.MEDIUM);
                repaint();
            } else if (btnResLarge.contains(p)) {
                resolution = FieldResolution.LARGE;
                parentFrame.updateResolution(FieldResolution.LARGE);
                repaint();
            } else if (btnStart.contains(p)) {
                startConfiguredMatch();
                repaint();
            }
        } else if (screen == Screen.MATCH) {
            MatchView match = game.getMatch();
            if (match != null && match.getStatus() == GameStatus.PAUSED && btnSaveGame.contains(p)) {
                saveCurrentMatch();
                repaint();
            } else if (match != null && match.getStatus() == GameStatus.FINISHED && btnMenu.contains(p)) {
                parentFrame.setCurrentScreen(Screen.MENU);
                repaint();
            }
        }
    }

    /*
     * Opens a file chooser dialog to save the currently paused match.
     */
    private void saveCurrentMatch() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar partida de DOPOSoccer");
        chooser.setFileFilter(new FileNameExtensionFilter("Partidas DOPOSoccer (*.dat)", "dat"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".dat")) {
                file = new File(file.getParentFile(), file.getName() + ".dat");
            }
            try {
                game.save(file);
                JOptionPane.showMessageDialog(this, "Partida guardada correctamente.", "Guardar", JOptionPane.INFORMATION_MESSAGE);
            } catch (DOPOSoccerException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al guardar", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /*
     * Opens a file chooser dialog to load a previously saved match.
     */
    private void openSavedMatch() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Abrir partida de DOPOSoccer");
        chooser.setFileFilter(new FileNameExtensionFilter("Partidas DOPOSoccer (*.dat)", "dat"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                game.open(chooser.getSelectedFile());
                this.lastScorerText = "¡Anotación!";
                this.winnerNotification = "Fin del juego";
                parentFrame.setCurrentScreen(Screen.MATCH);
            } catch (DOPOSoccerException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al abrir", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
