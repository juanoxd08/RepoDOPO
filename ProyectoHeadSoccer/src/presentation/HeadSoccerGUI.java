package presentation;

import domain.*;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

/**
 * Main GUI window and application entry point for the POOBSoccer game.
 * 
 * @author Juan Esppitia
 * @author Cristian Salamanca
 */
public class HeadSoccerGUI extends JFrame implements KeyListener, ActionListener {
    private HeadSoccer game;
    private PhotoHeadSoccer photo;
    private Timer gameLoop;
    private Set<Integer> activeKeys;

    /**
     * Constructs the main application window and starts the game loop timer.
     */
    public HeadSoccerGUI() {
        super("POOBSoccer 2026-2 - Head Soccer Arcade");
        this.game = new HeadSoccer();
        this.photo = new PhotoHeadSoccer(game);
        this.activeKeys = new HashSet<>();

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.add(photo, BorderLayout.CENTER);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setResizable(true);

        this.addKeyListener(this);
        this.setFocusable(true);

        this.gameLoop = new Timer(20, this);
        this.gameLoop.start();
    }

    /**
     * Executes one game loop tick, processing continuous inputs and repainting the view.
     * 
     * @param e Action event triggered by timer
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        processContinuousKeys();
        game.tick();
        photo.repaint();
    }

    /*
     * Dispatches active keyboard keys to player controllers polymorphically.
     */
    private void processContinuousKeys() {
        if (game.getState() != GameStatus.PLAYING && game.getState() != GameStatus.GOLDEN_GOAL) return;

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

    /**
     * Records pressed keys and triggers global shortcuts such as pause and reset.
     * 
     * @param e Key event
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        activeKeys.add(code);

        if (code == KeyEvent.VK_P) {
            game.togglePause();
        } else if (code == KeyEvent.VK_R) {
            game.reset();
        }
    }

    /**
     * Removes released keys from active tracking.
     * 
     * @param e Key event
     */
    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        activeKeys.remove(code);
    }

    /**
     * Handles typed key events.
     * 
     * @param e Key event
     */
    @Override
    public void keyTyped(KeyEvent e) {
    }

    /**
     * Main application entry point launching the Swing graphical interface.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HeadSoccerGUI ventana = new HeadSoccerGUI();
            ventana.setVisible(true);
        });
    }
}