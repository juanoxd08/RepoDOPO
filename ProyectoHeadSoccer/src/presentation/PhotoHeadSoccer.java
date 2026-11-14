package presentation;

import domain.*;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Graphical canvas rendering all screens and match entities according to the state machine:
 * - MENU
 * - SELECT_PLAYERS
 * - SELECT_CAMP
 * - PLAYING
 * - PAUSED
 * - GOAL
 * - DRAW / SUDDEN_DEATH
 * - FINISHED
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class PhotoHeadSoccer extends JPanel {
    private HeadSoccer game;
    private HeadSoccerGUI parentFrame;

    // Interactive button bounds for mouse clicks
    public final java.awt.Rectangle btnNewGame = new java.awt.Rectangle(0, 0, 0, 0);
    public final java.awt.Rectangle btnContinue = new java.awt.Rectangle(0, 0, 0, 0);
    public final java.awt.Rectangle btnStart = new java.awt.Rectangle(0, 0, 0, 0);
    public final java.awt.Rectangle btnMenu = new java.awt.Rectangle(0, 0, 0, 0);

    // Option toggle buttons
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

    public PhotoHeadSoccer(HeadSoccer game, HeadSoccerGUI parentFrame) {
        this.game = game;
        this.parentFrame = parentFrame;
        int screenW = game.getResolution().getPixelWidth();
        int screenH = game.getResolution().getPixelHeight();
        this.setPreferredSize(new Dimension(screenW, screenH));
        this.setBackground(new Color(135, 206, 235));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GameStatus status = game.getState();

        switch (status) {
            case MENU:
                drawMenuScreen(g2d);
                break;
            case SELECT_PLAYERS:
                drawSelectPlayersScreen(g2d);
                break;
            case SELECT_CAMP:
                drawSelectCampScreen(g2d);
                break;
            case PLAYING:
            case PAUSED:
            case GOAL_SCORED:
            case DRAW:
            case SUDDEN_DEATH:
            case FINISHED:
                drawMatchScreen(g2d, status);
                break;
        }
    }

    /* =========================================================================
     *  PANTALLA 1: MENÚ PRINCIPAL (HEAD SOCCER GAME / DOPOSoccer)
     * ========================================================================= */
    private void drawMenuScreen(Graphics2D g2d) {
        int w = getWidth();
        int h = getHeight();

        // Fondo de campo decorativo
        drawBackgroundField(g2d);

        // Capa oscura translúcida
        g2d.setColor(new Color(0, 0, 0, 140));
        g2d.fillRect(0, 0, w, h);

        // Banner del título
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

        // Botón "NEW GAME"
        btnNewGame.setBounds(w / 2 - 130, h / 2 - 25, 260, 56);
        drawGlossyButton(g2d, btnNewGame, "NEW GAME", new Color(245, 175, 25), Color.BLACK, 24);

        // Información de controles y bienvenida
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

    /* =========================================================================
     *  PANTALLA 2: SELECCIÓN DE JUGADORES (SELECT PLAYERS)
     * ========================================================================= */
    private void drawSelectPlayersScreen(Graphics2D g2d) {
        int w = getWidth();
        int h = getHeight();

        drawBackgroundField(g2d);

        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRect(0, 0, w, h);

        // Título "SELECT PLAYERS"
        g2d.setFont(new Font("Impact", Font.BOLD, 42));
        g2d.setColor(Color.YELLOW);
        String title = "SELECT PLAYERS";
        g2d.drawString(title, w / 2 - g2d.getFontMetrics().stringWidth(title) / 2, 55);

        // Panel de configuración
        int panelW = Math.min(760, w - 40);
        int panelH = h - 160;
        int px = w / 2 - panelW / 2;
        int py = 75;

        g2d.setColor(new Color(25, 30, 40, 230));
        g2d.fillRoundRect(px, py, panelW, panelH, 20, 20);
        g2d.setColor(new Color(255, 200, 50));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(px, py, panelW, panelH, 20, 20);

        // 1. Selector de Modalidad (PvP vs PvM)
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Modalidad:", px + 30, py + 40);

        btnModePvP.setBounds(px + 140, py + 18, 160, 34);
        drawToggleOption(g2d, btnModePvP, "Jugador vs Jugador", !game.isVsMachine());

        btnModePvM.setBounds(px + 320, py + 18, 160, 34);
        drawToggleOption(g2d, btnModePvM, "Jugador vs Máquina", game.isVsMachine());

        // Si es vs Máquina, selector de perfil
        if (game.isVsMachine()) {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawString("Perfil IA:", px + 500, py + 40);
            btnNextProfile.setBounds(px + 580, py + 18, 140, 34);
            drawToggleOption(g2d, btnNextProfile, game.getMachineProfile().getDisplayName() + " ↻", true);
        }

        // 2. Selector de Personajes / Países
        int colW = panelW / 2 - 40;

        // Jugador 1
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
        g2d.drawString("País: " + game.getPlayer1Country().getDisplayName(), p1X + 15, p1Y + 65);
        g2d.drawString("Poder: " + game.getPlayer1Country().getPowerName(), p1X + 15, p1Y + 95);

        btnNextP1Country.setBounds(p1X + 15, p1Y + 115, colW - 30, 32);
        drawGlossyButton(g2d, btnNextP1Country, "Cambiar Personaje ↻", new Color(70, 130, 180), Color.WHITE, 14);

        // Jugador 2
        int p2X = px + panelW / 2 + 10;
        int p2Y = py + 75;
        g2d.setColor(new Color(40, 45, 60));
        g2d.fillRoundRect(p2X, p2Y, colW, 160, 15, 15);
        g2d.setColor(new Color(100, 150, 255));
        g2d.drawRoundRect(p2X, p2Y, colW, 160, 15, 15);

        g2d.setFont(new Font("Arial", Font.BOLD, 17));
        g2d.setColor(Color.WHITE);
        g2d.drawString(game.isVsMachine() ? "MÁQUINA (Derecha)" : "JUGADOR 2 (Derecha)", p2X + 15, p2Y + 30);

        g2d.setFont(new Font("Arial", Font.PLAIN, 15));
        g2d.drawString("País: " + game.getPlayer2Country().getDisplayName(), p2X + 15, p2Y + 65);
        g2d.drawString("Poder: " + game.getPlayer2Country().getPowerName(), p2X + 15, p2Y + 95);

        btnNextP2Country.setBounds(p2X + 15, p2Y + 115, colW - 30, 32);
        drawGlossyButton(g2d, btnNextP2Country, "Cambiar Personaje ↻", new Color(70, 130, 180), Color.WHITE, 14);

        // 3. Tipo de Partido
        int ty = py + 255;
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Tipo de Partido:", px + 30, ty + 25);

        btnTypeTime.setBounds(px + 170, ty + 5, 200, 34);
        drawToggleOption(g2d, btnTypeTime, "Por Tiempo (" + game.getMatchDurationMinutes() + " min) ↻", game.getMatchType() == MatchType.TIME);

        btnTypeGoals.setBounds(px + 390, ty + 5, 200, 34);
        drawToggleOption(g2d, btnTypeGoals, "Por Goles (" + game.getTargetGoals() + " goles) ↻", game.getMatchType() == MatchType.GOALS);

        // Botón "CONTINUE"
        btnContinue.setBounds(w / 2 - 120, h - 70, 240, 48);
        drawGlossyButton(g2d, btnContinue, "CONTINUE ➔", new Color(245, 175, 25), Color.BLACK, 20);
    }

    /* =========================================================================
     *  PANTALLA 3: SELECCIÓN DE CAMPO (SELECT THE CAMP / RESOLUCIÓN)
     * ========================================================================= */
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
        drawToggleOption(g2d, btnResSmall, FieldResolution.SMALL.getLabel(), game.getResolution() == FieldResolution.SMALL);

        btnResMed.setBounds(px + 50, py + 125, 500, 42);
        drawToggleOption(g2d, btnResMed, FieldResolution.MEDIUM.getLabel(), game.getResolution() == FieldResolution.MEDIUM);

        btnResLarge.setBounds(px + 50, py + 180, 500, 42);
        drawToggleOption(g2d, btnResLarge, FieldResolution.LARGE.getLabel(), game.getResolution() == FieldResolution.LARGE);

        // Botón "START"
        btnStart.setBounds(w / 2 - 130, h - 85, 260, 54);
        drawGlossyButton(g2d, btnStart, "⚽ START ⚽", new Color(40, 180, 40), Color.WHITE, 24);
    }

    /* =========================================================================
     *  PANTALLA 4: PARTIDO EN JUEGO (STATE PLAYING, PAUSE, GOAL, DRAW, FINISHED)
     * ========================================================================= */
    private void drawMatchScreen(Graphics2D g2d, GameStatus status) {
        double scaleX = (double) getWidth() / Field.WIDTH;
        double scaleY = (double) getHeight() / Field.HEIGHT;
        double currentScale = Math.min(scaleX, scaleY);

        // Renderizado del campo y entidades a escala matemática
        Graphics2D gField = (Graphics2D) g2d.create();
        gField.scale(currentScale, currentScale);

        drawField(gField);

        if (game.getPlayer1() != null) drawPlayer(gField, game.getPlayer1());
        if (game.getPlayer2() != null) drawPlayer(gField, game.getPlayer2());
        drawBall(gField);

        gField.dispose();

        // Renderizado del HUD superior en resolución completa
        drawHUD(g2d);

        // Overlays según máquina de estados
        if (status == GameStatus.PAUSED) {
            drawPauseOverlay(g2d);
        } else if (status == GameStatus.GOAL_SCORED) {
            drawGoalOverlay(g2d);
        } else if (status == GameStatus.DRAW || status == GameStatus.SUDDEN_DEATH) {
            drawDrawOverlay(g2d);
        } else if (status == GameStatus.FINISHED) {
            drawFinishedOverlay(g2d);
        }
    }

    private void drawBackgroundField(Graphics2D g2d) {
        int w = getWidth();
        int h = getHeight();
        // Cielo
        g2d.setColor(new Color(135, 206, 235));
        g2d.fillRect(0, 0, w, h);
        // Gradas
        g2d.setColor(new Color(90, 110, 130));
        g2d.fillRect(0, h / 3, w, h / 3);
        // Césped
        g2d.setColor(new Color(46, 139, 87));
        g2d.fillRect(0, (int) (h * 0.65), w, (int) (h * 0.35));
    }

    public void drawField(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(new Color(95, 115, 135));
        g2d.fillRect(0, 0, Field.WIDTH, 68);

        g2d.setColor(new Color(35, 35, 35));
        g2d.fillRect(0, 68, Field.WIDTH, 14);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 7));
        g2d.drawString("DOPOSoccer 2026-2  •  HEAD SOCCER", 15, 78);
        g2d.drawString("ESCUELA COLOMBIANA DE INGENIERÍA", 190, 78);

        g2d.setColor(new Color(46, 139, 87));
        g2d.fillRect(0, 82, Field.WIDTH, Field.HEIGHT - 82);

        g2d.setColor(new Color(56, 155, 98));
        for (int i = 0; i < Field.WIDTH; i += 30) {
            g2d.fillRect(i, 82, 15, Field.HEIGHT - 82);
        }

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawLine(0, Field.HEIGHT - 1, Field.WIDTH, Field.HEIGHT - 1);
        g2d.drawLine(Field.WIDTH / 2, 82, Field.WIDTH / 2, Field.HEIGHT);
        g2d.drawOval(Field.WIDTH / 2 - 25, 120, 50, 40);

        if (game.getField() != null) {
            drawGoal(g2d, game.getField().getLeftGoal());
            drawGoal(g2d, game.getField().getRightGoal());
        }
    }

    private void drawGoal(Graphics2D g2d, Goal goal) {
        if (goal == null) return;
        domain.Rectangle area = goal.getGoalArea();
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

        domain.Rectangle bar = goal.getCrossBar();
        if (bar != null) {
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2f));
            g2d.drawRect((int) bar.getX(), (int) bar.getY(), (int) bar.getWidth(), (int) bar.getHeight());
        }
    }

    public void drawPlayer(Graphics g, Player p) {
        if (p == null || p.getHead() == null) return;
        Graphics2D g2d = (Graphics2D) g;

        Circle hc = p.getHead().getCircle();
        int hx = (int) hc.getX();
        int hy = (int) hc.getY();
        int hr = (int) hc.getRadius();

        Color baseColor = getColorByCountry(p.getCountry());

        // 1. Aura resplandeciente si tiene poder activado
        if (p.getPower() != null && p.getPower().isAuraActive()) {
            g2d.setColor(new Color(255, 220, 50, 90));
            g2d.fillOval(hx - hr - 10, hy - hr - 10, (hr + 10) * 2, (hr + 10) * 2);
            g2d.setColor(new Color(255, 160, 0, 140));
            g2d.setStroke(new BasicStroke(2f));
            g2d.drawOval(hx - hr - 6, hy - hr - 6, (hr + 6) * 2, (hr + 6) * 2);
        }

        // 2. Efecto de Aturdimiento / Vida en Cero o Efectos de poder
        if (p.isDisabled()) {
            g2d.setColor(new Color(255, 255, 255, 180));
            g2d.setFont(new Font("Arial", Font.BOLD, 7));
            String tag = (p.getCurrentEffect() != PlayerEffect.NONE) ? p.getCurrentEffect().name() : "STUN";
            g2d.drawString("★ " + tag + " ★", hx - 14, hy - hr - 8);
        }

        // 3. Cuerpo
        if (p.getBody() != null && p.getBody().getRectangle() != null) {
            domain.Rectangle br = p.getBody().getRectangle();
            g2d.setColor(baseColor.darker());
            g2d.fillRoundRect((int) br.getX(), (int) br.getY(), (int) br.getWidth(), (int) br.getHeight(), 5, 5);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1f));
            g2d.drawRoundRect((int) br.getX(), (int) br.getY(), (int) br.getWidth(), (int) br.getHeight(), 5, 5);
        }

        // 4. Cabeza
        g2d.setColor(baseColor);
        g2d.fillOval(hx - hr, hy - hr, hr * 2, hr * 2);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawOval(hx - hr, hy - hr, hr * 2, hr * 2);

        // Ojo
        g2d.setColor(Color.WHITE);
        int eyeX = (p.getSide() == Side.LEFT) ? hx + hr / 3 : hx - hr / 3;
        g2d.fillOval(eyeX - 3, hy - 3, 6, 7);
        g2d.setColor(Color.BLACK);
        g2d.fillOval(eyeX - (p.getSide() == Side.LEFT ? 1 : 2), hy - 2, 3, 4);

        // 5. Pie
        if (p.getFoot() != null && p.getFoot().getCircle() != null) {
            Circle fc = p.getFoot().getCircle();
            int fx = (int) fc.getX();
            int fy = (int) fc.getY();
            int fr = (int) fc.getRadius();
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillOval(fx - fr, fy - fr, fr * 2, fr * 2);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(fx - fr, fy - fr, fr * 2, fr * 2);
        }
    }

    private Color getColorByCountry(Country c) {
        if (c == null) return Color.RED;
        switch (c) {
            case SOUTH_KOREA: return new Color(230, 60, 60);
            case CAMEROON: return new Color(30, 160, 80);
            case NIGERIA: return new Color(20, 130, 60);
            case USA: return new Color(60, 100, 220);
            case COLOMBIA: return new Color(245, 205, 30);
            case CANADA: return new Color(220, 40, 40);
            case BRAZIL: return new Color(40, 170, 70);
            case ARGENTINA: return new Color(110, 180, 240);
            default: return Color.ORANGE;
        }
    }

    public void drawBall(Graphics g) {
        Ball ball = game.getBall();
        if (ball == null || ball.getCircle() == null) return;
        Graphics2D g2d = (Graphics2D) g;

        // Si hay poder de Ilusión activo, dibujar clones
        if (game.getPlayer1() != null && game.getPlayer1().getPower() instanceof IllusionPower) {
            drawIllusionClones(g2d, (IllusionPower) game.getPlayer1().getPower());
        }
        if (game.getPlayer2() != null && game.getPlayer2().getPower() instanceof IllusionPower) {
            drawIllusionClones(g2d, (IllusionPower) game.getPlayer2().getPower());
        }

        Circle bc = ball.getCircle();
        int bx = (int) bc.getX();
        int by = (int) bc.getY();
        int r = (int) bc.getRadius();

        boolean isPower = (game.getPlayer1() != null && game.getPlayer1().getPower() != null && game.getPlayer1().getPower().isPowerShotActive()) ||
                          (game.getPlayer2() != null && game.getPlayer2().getPower() != null && game.getPlayer2().getPower().isPowerShotActive());

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

    private void drawIllusionClones(Graphics2D g2d, IllusionPower illusion) {
        if (!illusion.hasActiveIllusion()) return;
        List<Circle> clones = illusion.getFakeBalls();
        for (Circle c : clones) {
            int bx = (int) c.getX();
            int by = (int) c.getY();
            int r = (int) c.getRadius();
            g2d.setColor(new Color(255, 255, 255, 210));
            g2d.fillOval(bx - r, by - r, r * 2, r * 2);
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.setStroke(new BasicStroke(1.2f));
            g2d.drawOval(bx - r, by - r, r * 2, r * 2);
            g2d.drawLine(bx - r + 2, by, bx + r - 2, by);
            g2d.drawLine(bx, by - r + 2, bx, by + r - 2);
        }
    }

    /* =========================================================================
     *  HUD SUPERIOR: BARRAS DE VIDA, PODER, MARCADOR Y TIMER
     * ========================================================================= */
    public void drawHUD(Graphics2D g2d) {
        int w = getWidth();

        // 1. Marcador central
        g2d.setColor(new Color(15, 20, 25, 230));
        g2d.fillRoundRect(w / 2 - 160, 10, 320, 65, 14, 14);
        g2d.setColor(new Color(245, 195, 35));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(w / 2 - 160, 10, 320, 65, 14, 14);

        int[] points = (game.getScoreBoard() != null) ? game.getScoreBoard().getPoints() : new int[]{0, 0};
        g2d.setFont(new Font("Impact", Font.PLAIN, 36));
        g2d.setColor(Color.WHITE);
        String scoreText = points[0] + "  -  " + points[1];
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(scoreText, w / 2 - fm.stringWidth(scoreText) / 2, 48);

        // Timer o Meta de goles
        g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2d.setColor(Color.GREEN);
        String centerInfo = "";
        if (game.getState() == GameStatus.SUDDEN_DEATH) {
            centerInfo = "GOL DE ORO";
            g2d.setColor(Color.YELLOW);
        } else if (game.getCurrentMatch() != null && game.getCurrentMatch().getMatchType() == MatchType.GOALS) {
            centerInfo = "META: " + game.getCurrentMatch().getTargetGoals() + " GOLES";
        } else if (game.getTimer() != null) {
            centerInfo = game.getTimer().getFormattedTime();
        }
        g2d.drawString(centerInfo, w / 2 - g2d.getFontMetrics().stringWidth(centerInfo) / 2, 68);

        // 2. HUD Jugador 1 (Izquierda)
        Player p1 = game.getPlayer1();
        if (p1 != null) {
            int p1X = 25;
            int p1Y = 14;
            drawPlayerHUD(g2d, p1, p1X, p1Y, 220, true);
        }

        // 3. HUD Jugador 2 (Derecha)
        Player p2 = game.getPlayer2();
        if (p2 != null) {
            int p2X = w - 245;
            int p2Y = 14;
            drawPlayerHUD(g2d, p2, p2X, p2Y, 220, false);
        }
    }

    private void drawPlayerHUD(Graphics2D g2d, Player p, int x, int y, int barW, boolean leftSide) {
        g2d.setFont(new Font("Arial", Font.BOLD, 13));
        g2d.setColor(Color.WHITE);
        String name = p.getName() + " (" + p.getCountry().getDisplayName() + ")";
        g2d.drawString(name, x, y + 12);

        // Barra de Vida
        int hpY = y + 18;
        g2d.setColor(Color.BLACK);
        g2d.fillRect(x, hpY, barW, 14);

        double hpRatio = Math.max(0.0, Math.min(1.0, p.getVidaActual() / p.getVidaMax()));
        Color hpColor = (hpRatio > 0.5) ? new Color(40, 200, 40) : (hpRatio > 0.25 ? Color.ORANGE : Color.RED);
        g2d.setColor(hpColor);
        g2d.fillRect(x + 1, hpY + 1, (int) ((barW - 2) * hpRatio), 12);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(x, hpY, barW, 14);

        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        String hpText = (int) p.getVidaActual() + "/" + (int) p.getVidaMax() + " HP";
        g2d.drawString(hpText, x + barW / 2 - g2d.getFontMetrics().stringWidth(hpText) / 2, hpY + 11);

        // Barra de Poder
        int powY = hpY + 16;
        g2d.setColor(Color.BLACK);
        g2d.fillRect(x, powY, barW, 10);

        double powRatio = (p.getPower() != null) ? (p.getPower().getCharge() / 100.0) : 0.0;
        powRatio = Math.max(0.0, Math.min(1.0, powRatio));
        g2d.setColor(powRatio >= 1.0 ? Color.YELLOW : new Color(240, 140, 0));
        g2d.fillRect(x + 1, powY + 1, (int) ((barW - 2) * powRatio), 8);
        g2d.setColor(Color.GRAY);
        g2d.drawRect(x, powY, barW, 10);

        if (p.getPower() != null && p.getPower().isAuraActive()) {
            g2d.setFont(new Font("Arial", Font.BOLD, 9));
            g2d.setColor(Color.YELLOW);
            g2d.drawString("⚡ AURA ACTIVA ⚡", x + barW / 2 - 40, powY + 9);
        }
    }

    /* =========================================================================
     *  OVERLAYS DE ESTADOS
     * ========================================================================= */
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
    }

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
        String desc = (game.getCurrentMatch() != null) ? game.getCurrentMatch().getLastScorerText() : "¡Anotación!";
        g2d.drawString(desc, w / 2 - g2d.getFontMetrics().stringWidth(desc) / 2, h / 2 + 40);
    }

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
        String winner = (game.getCurrentMatch() != null) ? game.getCurrentMatch().getWinnerNotification() : "Fin del juego";
        g2d.drawString(winner, w / 2 - g2d.getFontMetrics().stringWidth(winner) / 2, h / 2 - 10);

        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.setColor(Color.WHITE);
        String pts = "Recompensa: Ganador +100 puntos | Perdedor +25 puntos";
        g2d.drawString(pts, w / 2 - g2d.getFontMetrics().stringWidth(pts) / 2, h / 2 + 30);

        btnMenu.setBounds(w / 2 - 120, h / 2 + 65, 240, 48);
        drawGlossyButton(g2d, btnMenu, "VOLVER AL MENÚ", new Color(245, 175, 25), Color.BLACK, 18);
    }

    /* =========================================================================
     *  MÉTODOS AUXILIARES DE RENDERIZADO DE BOTONES INTERACTIVOS
     * ========================================================================= */
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

    public void handleMouseClick(MouseEvent e) {
        Point p = e.getPoint();
        GameStatus status = game.getState();

        if (status == GameStatus.MENU) {
            if (btnNewGame.contains(p)) {
                game.startNewGameFlow();
                repaint();
            }
        } else if (status == GameStatus.SELECT_PLAYERS) {
            if (btnModePvP.contains(p)) {
                game.setVsMachine(false);
                repaint();
            } else if (btnModePvM.contains(p)) {
                game.setVsMachine(true);
                repaint();
            } else if (btnNextProfile.contains(p) && game.isVsMachine()) {
                MachineProfile[] profiles = MachineProfile.values();
                int nextIdx = (game.getMachineProfile().ordinal() + 1) % profiles.length;
                game.setMachineProfile(profiles[nextIdx]);
                repaint();
            } else if (btnNextP1Country.contains(p)) {
                Country[] countries = {Country.SOUTH_KOREA, Country.CAMEROON, Country.NIGERIA, Country.USA, Country.COLOMBIA, Country.CANADA};
                int idx = 0;
                for (int i = 0; i < countries.length; i++) {
                    if (countries[i] == game.getPlayer1Country()) idx = (i + 1) % countries.length;
                }
                game.setPlayer1Country(countries[idx]);
                repaint();
            } else if (btnNextP2Country.contains(p)) {
                Country[] countries = {Country.CAMEROON, Country.NIGERIA, Country.USA, Country.SOUTH_KOREA, Country.COLOMBIA, Country.CANADA};
                int idx = 0;
                for (int i = 0; i < countries.length; i++) {
                    if (countries[i] == game.getPlayer2Country()) idx = (i + 1) % countries.length;
                }
                game.setPlayer2Country(countries[idx]);
                repaint();
            } else if (btnTypeTime.contains(p)) {
                game.setMatchType(MatchType.TIME);
                int[] durations = {1, 2, 3};
                int nextM = (game.getMatchDurationMinutes() % 3) + 1;
                game.setMatchDurationMinutes(nextM);
                repaint();
            } else if (btnTypeGoals.contains(p)) {
                game.setMatchType(MatchType.GOALS);
                int[] goals = {3, 5, 7};
                int nextG = (game.getTargetGoals() == 3) ? 5 : (game.getTargetGoals() == 5 ? 7 : 3);
                game.setTargetGoals(nextG);
                repaint();
            } else if (btnContinue.contains(p)) {
                game.confirmPlayersAndProceedToCamp();
                repaint();
            }
        } else if (status == GameStatus.SELECT_CAMP) {
            if (btnResSmall.contains(p)) {
                game.setResolution(FieldResolution.SMALL);
                parentFrame.updateResolution(FieldResolution.SMALL);
                repaint();
            } else if (btnResMed.contains(p)) {
                game.setResolution(FieldResolution.MEDIUM);
                parentFrame.updateResolution(FieldResolution.MEDIUM);
                repaint();
            } else if (btnResLarge.contains(p)) {
                game.setResolution(FieldResolution.LARGE);
                parentFrame.updateResolution(FieldResolution.LARGE);
                repaint();
            } else if (btnStart.contains(p)) {
                game.startActiveMatch();
                repaint();
            }
        } else if (status == GameStatus.FINISHED) {
            if (btnMenu.contains(p)) {
                game.returnToMenu();
                repaint();
            }
        }
    }
}
