package presentation;

import domain.*;
import persistence.Log;
import persistence.SerializedMatchRepository;
import persistence.SerializedProfileRepository;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * Main application window and controller coordinating the Swing event loop,
 * mouse interactions, keyboard navigation, and field resolution resizing.
 * 
 * @author Juan Espitia
 * @author Cristian Salamanca
 */
public class HeadSoccerGUI extends JFrame implements KeyListener, ActionListener {
    private DOPOSoccer game;
    private PhotoHeadSoccer photo;
    private CardLayout cardLayout;
    private JPanel cardsPanel;
    private SelectPlayersPanel selectPlayersPanel;
    private SelectCampPanel selectCampPanel;
    private Timer gameLoop;
    private Set<Integer> activeKeys;
    private Screen currentScreen;
    private UpgradesPanel upgradesPanel;
    private Screen returnScreen = Screen.SELECT_PLAYERS;

    /**
     * Constructs the main application window with Swing menu panels and match canvas.
     */
    public HeadSoccerGUI() {
        super("DOPOSoccer 2026-2 - Head Soccer Arcade");
        this.game = new DOPOSoccer(new SerializedMatchRepository(), new SerializedProfileRepository());
        this.currentScreen = Screen.MENU;
        this.photo = new PhotoHeadSoccer(game, this);
        this.activeKeys = new HashSet<>();

        this.cardLayout = new CardLayout();
        this.cardsPanel = new JPanel(cardLayout);
        this.cardsPanel.setPreferredSize(photo.getPreferredSize());

        MenuPanel menuPanel = new MenuPanel(
                () -> setCurrentScreen(Screen.SELECT_PLAYERS),
                () -> photo.openSavedMatch()
        );
        this.selectPlayersPanel = new SelectPlayersPanel(
                photo,
                () -> setCurrentScreen(Screen.MENU),
                () -> setCurrentScreen(Screen.SELECT_CAMP),
                () -> setCurrentScreen(Screen.UPGRADES)
        );
        this.selectCampPanel = new SelectCampPanel(
                photo,
                this::updateResolution,
                () -> setCurrentScreen(Screen.SELECT_PLAYERS),
                () -> photo.startConfiguredMatch()
        );

        this.upgradesPanel = new UpgradesPanel(game, photo,() -> setCurrentScreen(returnScreen));

        this.cardsPanel.add(menuPanel, Screen.MENU.name());
        this.cardsPanel.add(selectPlayersPanel, Screen.SELECT_PLAYERS.name());
        this.cardsPanel.add(selectCampPanel, Screen.SELECT_CAMP.name());
        this.cardsPanel.add(photo, Screen.MATCH.name());
        this.cardsPanel.add(upgradesPanel, Screen.UPGRADES.name());

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.add(cardsPanel, BorderLayout.CENTER);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setResizable(true);

        this.addKeyListener(this);
        this.setFocusable(true);

        this.photo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                photo.handleMouseClick(e);
                HeadSoccerGUI.this.requestFocusInWindow();
            }
        });

        setCurrentScreen(Screen.MENU);

        this.gameLoop = new Timer(Physics.TICK_MS, this);
        this.gameLoop.start();
    }

    /**
     * Returns the currently displayed navigation screen.
     *
     * @return Active screen
     */
    public Screen getCurrentScreen() {
        return currentScreen;
    }

    /**
     * Switches the visible Swing card to the requested navigation screen.
     *
     * @param screen Target screen
     */
    public void setCurrentScreen(Screen screen) {
        if(screen == Screen.UPGRADES){
            returnScreen = (this.currentScreen == Screen.MATCH) ? Screen.MENU : this.currentScreen;
        }
        this.currentScreen = screen;
        this.activeKeys.clear();
        if (screen == Screen.SELECT_PLAYERS && selectPlayersPanel != null) {
            selectPlayersPanel.refreshView();
        } else if (screen == Screen.SELECT_CAMP && selectCampPanel != null) {
            selectCampPanel.refreshView();
        } else if (screen == Screen.UPGRADES && upgradesPanel != null) {
            upgradesPanel.refreshView();
        }


        this.cardLayout.show(cardsPanel, screen.name());
        if (screen == Screen.MATCH) {
            this.requestFocusInWindow();
        }
    }

    /**
     * Resizes the canvas and window when selecting a different resolution.
     * 
     * @param res Selected FieldResolution
     */
    public void updateResolution(FieldResolution res) {
        Dimension size = new Dimension(res.getPixelWidth(), res.getPixelHeight());
        photo.setPreferredSize(size);
        cardsPanel.setPreferredSize(size);
        this.pack();
        this.setLocationRelativeTo(null);
    }

    /**
     * Executes one frame of the Swing timer game loop, advancing domain physics and repainting.
     *
     * @param e Timer action event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentScreen == Screen.MATCH) {
            try {
                processContinuousKeys();
                game.tick();
            } catch (RuntimeException ex) {
                Log.record(ex);
                game.pause();
                JOptionPane.showMessageDialog(
                        this,
                        "Ocurrió un error inesperado: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
        photo.repaint();
    }

    /*
     * Translates currently held keyboard keys into player action sets for both sides.
     */
    private void processContinuousKeys() {
        MatchView match = game.getMatch();
        if (match == null) return;
        GameStatus status = match.getStatus();
        if (status != GameStatus.PLAYING && status != GameStatus.SUDDEN_DEATH) return;

        Set<PlayerAction> p1Actions = new HashSet<>();
        if (activeKeys.contains(KeyEvent.VK_A)) p1Actions.add(PlayerAction.LEFT);
        if (activeKeys.contains(KeyEvent.VK_D)) p1Actions.add(PlayerAction.RIGHT);
        if (activeKeys.contains(KeyEvent.VK_W)) p1Actions.add(PlayerAction.JUMP);
        if (activeKeys.contains(KeyEvent.VK_S)) p1Actions.add(PlayerAction.KICK);
        if (activeKeys.contains(KeyEvent.VK_Q)) p1Actions.add(PlayerAction.POWER);
        game.setIntention(Side.LEFT, p1Actions);

        Set<PlayerAction> p2Actions = new HashSet<>();
        if (activeKeys.contains(KeyEvent.VK_LEFT)) p2Actions.add(PlayerAction.LEFT);
        if (activeKeys.contains(KeyEvent.VK_RIGHT)) p2Actions.add(PlayerAction.RIGHT);
        if (activeKeys.contains(KeyEvent.VK_UP)) p2Actions.add(PlayerAction.JUMP);
        if (activeKeys.contains(KeyEvent.VK_DOWN)) p2Actions.add(PlayerAction.KICK);
        if (activeKeys.contains(KeyEvent.VK_CONTROL)) p2Actions.add(PlayerAction.POWER);
        game.setIntention(Side.RIGHT, p2Actions);
    }

    /**
     * Handles keyboard press events for navigation shortcuts, pause, restart, and continuous controls.
     *
     * @param e Keyboard press event
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        activeKeys.add(code);

        if (currentScreen == Screen.MENU) {
            if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
                setCurrentScreen(Screen.SELECT_PLAYERS);
            }
            return;
        }
        if (currentScreen == Screen.SELECT_PLAYERS) {
            if (code == KeyEvent.VK_ENTER) {
                setCurrentScreen(Screen.SELECT_CAMP);
            }
            return;
        }
        if (currentScreen == Screen.SELECT_CAMP) {
            if (code == KeyEvent.VK_ENTER) {
                photo.startConfiguredMatch();
            }
            return;
        }

        MatchView match = game.getMatch();
        if (match == null) return;
        GameStatus status = match.getStatus();

        if (status == GameStatus.PLAYING || status == GameStatus.SUDDEN_DEATH) {
            if (code == KeyEvent.VK_P) {
                game.togglePause();
            } else if (code == KeyEvent.VK_R) {
                photo.startConfiguredMatch();
            } else if (code == KeyEvent.VK_M) {
                game.finish();
                setCurrentScreen(Screen.MENU);
            }
        } else if (status == GameStatus.PAUSED) {
            if (code == KeyEvent.VK_P) {
                game.togglePause();
            } else if (code == KeyEvent.VK_M) {
                game.finish();
                setCurrentScreen(Screen.MENU);
            }
        } else if (status == GameStatus.FINISHED) {
            if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_M || code == KeyEvent.VK_SPACE) {
                setCurrentScreen(Screen.MENU);
            }
        }
    }

    /**
     * Removes a released key from the active continuous-input set.
     *
     * @param e Keyboard release event
     */
    @Override
    public void keyReleased(KeyEvent e) {
        activeKeys.remove(e.getKeyCode());
    }

    /**
     * Unused key-typed callback required by KeyListener.
     *
     * @param e Keyboard typed event
     */
    @Override
    public void keyTyped(KeyEvent e) {
    }

    /**
     * Application entry point launching the DOPOSoccer Swing window on the EDT.
     *
     * @param args Command-line arguments (unused)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HeadSoccerGUI ventana = new HeadSoccerGUI();
            ventana.setVisible(true);
        });
    }
}
