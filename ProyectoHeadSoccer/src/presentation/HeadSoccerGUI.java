package presentation;

import domain.*;

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
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class HeadSoccerGUI extends JFrame implements KeyListener, ActionListener {
    private HeadSoccer game;
    private PhotoHeadSoccer photo;
    private Timer gameLoop;
    private Set<Integer> activeKeys;

    public HeadSoccerGUI() {
        super("DOPOSoccer 2026-2 - Head Soccer Arcade");
        this.game = new HeadSoccer();
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

        // Escuchador de clics del ratón para botones de interfaz
        this.photo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                photo.handleMouseClick(e);
                HeadSoccerGUI.this.requestFocusInWindow();
            }
        });

        // Bucle a 60 FPS (16 ms por frame)
        this.gameLoop = new Timer(16, this);
        this.gameLoop.start();
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
        processContinuousKeys();
        game.tick();
        photo.repaint();
    }

    private void processContinuousKeys() {
        if (game.getState() != GameStatus.PLAYING && game.getState() != GameStatus.SUDDEN_DEATH) return;

        Set<PlayerAction> p1Actions = new HashSet<>();
        if (activeKeys.contains(KeyEvent.VK_A)) p1Actions.add(PlayerAction.LEFT);
        if (activeKeys.contains(KeyEvent.VK_D)) p1Actions.add(PlayerAction.RIGHT);
        if (activeKeys.contains(KeyEvent.VK_W)) p1Actions.add(PlayerAction.JUMP);
        if (activeKeys.contains(KeyEvent.VK_S)) p1Actions.add(PlayerAction.KICK);
        if (activeKeys.contains(KeyEvent.VK_Q)) p1Actions.add(PlayerAction.POWER);

        if (game.getController1() != null) {
            game.getController1().receive(p1Actions);
        }

        Set<PlayerAction> p2Actions = new HashSet<>();
        if (activeKeys.contains(KeyEvent.VK_LEFT)) p2Actions.add(PlayerAction.LEFT);
        if (activeKeys.contains(KeyEvent.VK_RIGHT)) p2Actions.add(PlayerAction.RIGHT);
        if (activeKeys.contains(KeyEvent.VK_UP)) p2Actions.add(PlayerAction.JUMP);
        if (activeKeys.contains(KeyEvent.VK_DOWN)) p2Actions.add(PlayerAction.KICK);
        if (activeKeys.contains(KeyEvent.VK_CONTROL)) p2Actions.add(PlayerAction.POWER);

        if (game.getController2() != null) {
            game.getController2().receive(p2Actions);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        activeKeys.add(code);

        GameStatus status = game.getState();

        // Navegación por teclado según el flujo de la máquina de estados
        if (status == GameStatus.MENU) {
            if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
                game.startNewGameFlow();
            }
        } else if (status == GameStatus.SELECT_PLAYERS) {
            if (code == KeyEvent.VK_ENTER) {
                game.confirmPlayersAndProceedToCamp();
            }
        } else if (status == GameStatus.SELECT_CAMP) {
            if (code == KeyEvent.VK_ENTER) {
                game.startActiveMatch();
            }
        } else if (status == GameStatus.PLAYING || status == GameStatus.SUDDEN_DEATH) {
            if (code == KeyEvent.VK_P) {
                game.togglePause();
            } else if (code == KeyEvent.VK_R) {
                game.reset();
            } else if (code == KeyEvent.VK_M) {
                game.returnToMenu();
            }
        } else if (status == GameStatus.PAUSED) {
            if (code == KeyEvent.VK_P) {
                game.togglePause();
            } else if (code == KeyEvent.VK_M) {
                game.returnToMenu();
            }
        } else if (status == GameStatus.FINISHED) {
            if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_M || code == KeyEvent.VK_SPACE) {
                game.returnToMenu();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        activeKeys.remove(code);

        if (code == KeyEvent.VK_S && game.getPlayer1() != null) {
            game.getPlayer1().stopKick();
        }
        if (code == KeyEvent.VK_DOWN && game.getPlayer2() != null) {
            game.getPlayer2().stopKick();
        }
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
