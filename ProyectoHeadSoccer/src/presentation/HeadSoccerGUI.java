package presentation;

import domain.*;
import persistence.SerializedMatchRepository;

import javax.swing.*;
import java.awt.BorderLayout;
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
    private Timer gameLoop;
    private Set<Integer> activeKeys;
    private Screen currentScreen;

    public HeadSoccerGUI() {
        super("DOPOSoccer 2026-2 - Head Soccer Arcade");
        this.game = new DOPOSoccer(new SerializedMatchRepository());
        this.currentScreen = Screen.MENU;
        this.photo = new PhotoHeadSoccer(game, this);
        this.activeKeys = new HashSet<>();

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.add(photo, BorderLayout.CENTER);
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

        this.gameLoop = new Timer(Physics.TICK_MS, this);
        this.gameLoop.start();
    }

    public Screen getCurrentScreen() {
        return currentScreen;
    }

    public void setCurrentScreen(Screen screen) {
        this.currentScreen = screen;
    }

    /**
     * Resizes the canvas and window when selecting a different resolution.
     * 
     * @param res Selected FieldResolution
     */
    public void updateResolution(FieldResolution res) {
        photo.setPreferredSize(new Dimension(res.getPixelWidth(), res.getPixelHeight()));
        this.pack();
        this.setLocationRelativeTo(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentScreen == Screen.MATCH) {
            processContinuousKeys();
            game.tick();
        }
        photo.repaint();
    }

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

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        activeKeys.add(code);

        if (currentScreen == Screen.MENU) {
            if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
                currentScreen = Screen.SELECT_PLAYERS;
            }
            return;
        }
        if (currentScreen == Screen.SELECT_PLAYERS) {
            if (code == KeyEvent.VK_ENTER) {
                currentScreen = Screen.SELECT_CAMP;
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
                currentScreen = Screen.MENU;
            }
        } else if (status == GameStatus.PAUSED) {
            if (code == KeyEvent.VK_P) {
                game.togglePause();
            } else if (code == KeyEvent.VK_M) {
                game.finish();
                currentScreen = Screen.MENU;
            }
        } else if (status == GameStatus.FINISHED) {
            if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_M || code == KeyEvent.VK_SPACE) {
                currentScreen = Screen.MENU;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        activeKeys.remove(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HeadSoccerGUI ventana = new HeadSoccerGUI();
            ventana.setVisible(true);
        });
    }
}
