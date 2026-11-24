package presentation;

import domain.*;
import persistence.Log;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
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
    private final ImageAssets assets = new ImageAssets();
    private final MatchSetup setup = new MatchSetup();
    private final FieldRenderer fieldRenderer = new FieldRenderer();
    private final PlayerRenderer playerRenderer = new PlayerRenderer();
    private final BallRenderer ballRenderer = new BallRenderer(assets);
    private final HudRenderer hudRenderer = new HudRenderer(assets);
    private final SoundPlayer soundPlayer = new SoundPlayer();

    private String lastScorerText = "¡Anotación!";
    private String winnerNotification = "Fin del juego";

    public final java.awt.Rectangle btnSaveGame = new java.awt.Rectangle(0, 0, 0, 0);
    public final java.awt.Rectangle btnMenu = new java.awt.Rectangle(0, 0, 0, 0);
    public final java.awt.Rectangle btnUpgrades = new java.awt.Rectangle(0, 0, 0, 0);
    private final OverlayRenderer overlayRenderer = new OverlayRenderer(btnSaveGame, btnMenu, btnUpgrades);

    /**
     * Returns the match configuration model shared with setup screens.
     *
     * @return MatchSetup instance
     */
    public MatchSetup getSetup() {
        return setup;
    }

    /**
     * Toggles global match sound effects on or off.
     */
    public void toggleMute() {
        soundPlayer.toggleMute();
    }

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
        int screenW = setup.getResolution().getPixelWidth();
        int screenH = setup.getResolution().getPixelHeight();
        this.setPreferredSize(new Dimension(screenW, screenH));
        this.setBackground(new Color(135, 206, 235));
    }

    /**
     * Builds a MatchConfig from the current UI selections and starts a new match.
     */
    public void startConfiguredMatch() {
        try {
            MatchConfig config = setup.toConfig();
            this.lastScorerText = "¡Anotación!";
            this.winnerNotification = "Fin del juego";
            game.newMatch(config);
            parentFrame.setCurrentScreen(Screen.MATCH);
        } catch (DOPOSoccerException ex) {
            Log.record(ex);
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
        soundPlayer.play(Sound.GOAL);
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
        soundPlayer.play(Sound.STUNNED);
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
        soundPlayer.play(Sound.FINISH);
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
     * Plays the ball kick sound effect when a player kicks the ball.
     *
     * @param kicker Side of the kicking player
     */
    @Override
    public void onBallKicked(Side kicker) {
        soundPlayer.play(Sound.KICK);
    }

    /**
     * Plays the power aura sound effect when a player activates their power.
     *
     * @param side Side of the player activating power
     */
    @Override
    public void onPowerActivated(Side side) {
        soundPlayer.play(Sound.POWER_AURA);
    }

    /**
     * Plays the power-shot launch sound effect when a power shot begins.
     *
     * @param shooter Side of the shooting player
     * @param country Country of the shooting player
     */
    @Override
    public void onPowerShot(Side shooter, Country country) {
        soundPlayer.play(Sound.POWER_SHOT);
    }

    /**
     * Plays the power-hit sound effect when a power shot hits an opponent.
     *
     * @param victim Side of the hit opponent
     * @param damage Health damage dealt
     */
    @Override
    public void onPowerHit(Side victim, double damage) {
        soundPlayer.play(Sound.POWER_HIT);
    }

    /**
     * Notifies the user when persisting player profiles fails after match completion.
     *
     * @param message Error description
     */
    @Override
    public void onProfileSaveFailed(String message) {
        JOptionPane.showMessageDialog(
                this,
                "No se pudo guardar el progreso: " + message,
                "Error al guardar progreso",
                JOptionPane.ERROR_MESSAGE
        );
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

        MatchView match = game.getMatch();
        if (match != null) {
            drawMatchScreen(g2d, match);
        }
    }

    /*
     * Renders the active match screen, scaled pitch entities, HUD, and state overlays.
     */
    private void drawMatchScreen(Graphics2D g2d, MatchView match) {
        if (match == null) return;
        int w = getWidth();
        int h = getHeight();
        int fieldW = match.getFieldWidth();
        int fieldH = match.getFieldHeight();

        g2d.setColor(new Color(48, 72, 104));
        g2d.fillRect(0, 0, w, h);

        int hudReserve = (int) (h * 0.16);
        double scaleX = (double) w / fieldW;
        double scaleY = (double) (h - hudReserve) / fieldH;
        double currentScale = Math.min(scaleX, scaleY);

        int offsetX = (int) ((w - fieldW * currentScale) / 2.0);
        int offsetY = (int) (h - fieldH * currentScale);

        Graphics2D gField = (Graphics2D) g2d.create();
        gField.translate(offsetX, offsetY);
        gField.scale(currentScale, currentScale);

        fieldRenderer.draw(gField, match);

        PlayerView left = match.getPlayer(Side.LEFT);
        PlayerView right = match.getPlayer(Side.RIGHT);
        if (left != null) playerRenderer.draw(gField, left);
        if (right != null) playerRenderer.draw(gField, right);
        ballRenderer.draw(gField, match);

        gField.dispose();

        hudRenderer.draw(g2d, match, setup, w);
        overlayRenderer.draw(g2d, match, w, h, lastScorerText, winnerNotification);
    }

    /**
     * Dispatches mouse click events across match overlay buttons.
     *
     * @param e Mouse click event
     */
    public void handleMouseClick(MouseEvent e) {
        Point p = e.getPoint();
        Screen screen = parentFrame.getCurrentScreen();

        if (screen == Screen.MATCH) {
            MatchView match = game.getMatch();
            if (match != null && match.getStatus() == GameStatus.PAUSED && btnSaveGame.contains(p)) {
                saveCurrentMatch();
                repaint();
            } else if (match != null && match.getStatus() == GameStatus.FINISHED && btnUpgrades.contains(p)) {
                parentFrame.setCurrentScreen(Screen.UPGRADES);
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
                Log.record(ex);
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al guardar", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Opens a file chooser dialog to load a previously saved match.
     */
    public void openSavedMatch() {
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
                Log.record(ex);
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al abrir", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}
